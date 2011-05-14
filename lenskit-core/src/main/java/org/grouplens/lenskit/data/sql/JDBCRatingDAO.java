/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.data.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.AbstractLongCursor;
import org.grouplens.lenskit.data.AbstractRatingCursor;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.MutableRating;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.dao.AbstractRatingDataAccessObject;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating DAO backed by a JDBC connection.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class JDBCRatingDAO extends AbstractRatingDataAccessObject {
    public static class Manager implements DataAccessObjectManager<JDBCRatingDAO> {
        private final String cxnUrl;
        private final SQLStatementFactory factory;
        
        public Manager(String url, SQLStatementFactory config) {
            cxnUrl = url;
            factory = config;
        }
        
        @Override
        public JDBCRatingDAO open() {
            if (cxnUrl == null)
                throw new UnsupportedOperationException("Cannot open session w/o URL");
            
            Connection dbc;
            try {
                dbc = DriverManager.getConnection(cxnUrl);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new JDBCRatingDAO(new JDBCDataSession(dbc, factory), true);
        }
        
        public JDBCRatingDAO open(Connection existingConnection) {
            return new JDBCRatingDAO(new JDBCDataSession(existingConnection, factory), false);
        }
    }
    
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final JDBCDataSession session;
	protected final boolean ownsSession;
	
	public JDBCRatingDAO(JDBCDataSession session, boolean ownsSession) {
	    this.session = session;
	    this.ownsSession = ownsSession;
	}
    
    @Override
    public void close() {
        if (!ownsSession)
            return;
        
        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LongCursor getUsers() {
    	try {
    		PreparedStatement s = session.userStatement();
    		return new IDCursor(s);
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    protected int getCount(PreparedStatement s) throws SQLException {
    	ResultSet rs = null;
    	
        try {
        	rs = s.executeQuery();
        	if (!rs.next())
        		throw new RuntimeException("User count query returned no rows");
        	return rs.getInt(1);
        } finally {
        	if (rs != null)
        		rs.close();
        }
    }

    @Override
    public int getUserCount() {
    	try {
    		return getCount(session.userCountStatement());
        } catch (SQLException e) {
        	throw new RuntimeException(e);
        }
    }

    @Override
    public LongCursor getItems() {
    	try {
    		PreparedStatement s = session.itemStatement();
    		return new IDCursor(s);
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    }

    @Override
    public int getItemCount() {
    	try {
    		return getCount(session.itemCountStatement());
        } catch (SQLException e) {
        	throw new RuntimeException(e);
        }
    }

    @Override
    public Cursor<Rating> getRatings() {
        return getRatings(SortOrder.ANY);
    }

    @Override
    public Cursor<Rating> getRatings(SortOrder order) {
    	try {
    		PreparedStatement s = session.ratingStatement(order);
    		return new RatingCursor(s);
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    }

    @Override
    public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
    	try {
    		PreparedStatement s = session.userRatingStatement(order);
    		s.setLong(1, userId);
    		return new RatingCursor(s);
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    }

    @Override
    public Cursor<Rating> getItemRatings(long itemId, SortOrder order) {
    	try {
    		PreparedStatement s = session.itemRatingStatement(order);
    		s.setLong(1, itemId);
    		return new RatingCursor(s);
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    static class IDCursor extends AbstractLongCursor {
    	private Logger logger = LoggerFactory.getLogger(getClass());
    	private ResultSet rset;
    	private boolean advanced;
    	private boolean valid;
    	
    	public IDCursor(PreparedStatement stmt) throws SQLException {
    		advanced = false;
    		rset = stmt.executeQuery();
    	}
    	
    	public boolean hasNext() {
    		if (!advanced) {
    			try {
					valid = rset.next();
				} catch (SQLException e) {
					logger.error("Error fetching row", e);
				}
    			advanced = true;
    		}
    		return valid;
    	}
    	
    	public long nextLong() {
    		if (!hasNext())
    			throw new NoSuchElementException();
    		advanced = false;
    		try {
				return rset.getLong(1);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
    	}
    	
    	public void close() {
    		try {
    			rset.close();
    		} catch (SQLException e) {
    			throw new RuntimeException(e);
    		}
    	}
    }
    
    static class RatingCursor extends AbstractRatingCursor<Rating> {
    	private ResultSet resultSet;
    	private boolean hasTimestampColumn;
    	private MutableRating rating;
    	
    	public RatingCursor(PreparedStatement stmt) throws SQLException {
    		rating = new MutableRating();
    		resultSet = stmt.executeQuery();
    		try {
				hasTimestampColumn = resultSet.getMetaData().getColumnCount() > 3;
			} catch (SQLException e) {
				resultSet.close();
				throw e;
			} catch (RuntimeException e) {
				resultSet.close();
				throw e;
			}
    	}
    	
    	@Override
    	public Rating poll() {
    		try {
				if (!resultSet.next())
					return null;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
    		
    		try {
				rating.setUserId(resultSet.getLong(1));
				if (resultSet.wasNull())
					throw new RuntimeException("Unexpected null user ID");
				rating.setItemId(resultSet.getLong(2));
				if (resultSet.wasNull())
					throw new RuntimeException("Unexpected null item ID");
	    		rating.setRating(resultSet.getDouble(3));
	    		if (resultSet.wasNull())
					throw new RuntimeException("Unexpected null rating");
	    		long ts = -1;
	    		if (hasTimestampColumn) {
	    			ts = resultSet.getLong(4);
	    			if (resultSet.wasNull())
	    				ts = -1;
	    		}
	    		rating.setTimestamp(ts);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
    		
    		return rating;
    	}
    	
    	public void close() {
    		try {
				resultSet.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
    	}
    }

}
