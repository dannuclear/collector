package ru.bisoft.collector.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SZBDData {
    private Long id;
    private String snils;
    private String surname;
    private String name;
    private String patronymic;

    private LocalDate birthday;
    private String paperSeries;
    private String paperNumber;
    private String paperIssuer;
    private LocalDate paperIssueDate;
}
