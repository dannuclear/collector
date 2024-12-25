package ru.bisoft.collector;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "collector.db")
public class CollectorProperties {
    private List<String> urls;
}
