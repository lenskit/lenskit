package org.grouplens.reflens;

/**
 * Base class for recommender modules.  Provides access to the core component.
 * 
 * <p>Base classes must call {@link #configure()} to install the core module.
 * 
 * @see RecommenderModuleComponent
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderModule extends RecommenderModuleComponent {
	public final RecommenderCoreModule core;
	
	protected RecommenderModule() {
		core = new RecommenderCoreModule();
	}
	
	@Override
	protected void configure() {
		install(core);
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		core.setName(name);
	}

}
