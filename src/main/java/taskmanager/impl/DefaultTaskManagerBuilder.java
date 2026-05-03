package taskmanager.impl;

import taskmanager.api.TaskManager;

/**
 * Concrete builder for constructing a DefaultTaskManager.
 * Obtained via TaskManager.builder().
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {

    private String weatherApiKey = "";
    private String storagePath = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.weatherApiKey = apiKey;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        this.storagePath = path;
        return this;
    }

    /**
     * Builds a new DefaultTaskManager with the configured settings.
     *
     * @return a ready-to-use TaskManager
     */
    @Override
    public TaskManager build() {
        return new DefaultTaskManager(weatherApiKey);
    }
}
