package org.lenskit.build;

import org.gradle.api.internal.artifacts.mvnsettings.LocalMavenRepositoryLocator;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal;

import java.io.File;

/**
 * Custom Maven deployment class for testing deploys.
 */
public class CustomMavenDeploy extends PublishToMavenLocal {
    private Property<File> repository;

    public CustomMavenDeploy() {
        repository = getProject().getObjects().property(File.class);
    }

    public Property<File> getRepository() {
        return repository;
    }

    @Override
    protected LocalMavenRepositoryLocator getMavenRepositoryLocator() {
        return () -> repository.get();
    }
}
