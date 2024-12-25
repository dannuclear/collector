package ru.bisoft.collector.batch;

public class SQLHelper {
    public static final String SQL = """
            WITH personIds AS (
                SELECT ppi.key_person AS id, MAX(pp.key_person_paper) AS personPaperId FROM payment_accrual pa
                INNER JOIN payment ON (payment.key_payment = pa.key_payment AND date_trunc('month', payment.create_date_payment) > '01.06.2024')
                INNER JOIN person_payment_info ppi ON ppi.key_person_payment_info = pa.key_person_payment_info
                INNER JOIN person_paper pp ON pp.key_person = ppi.key_person
                INNER JOIN paper p ON (p.key_paper = pp.key_paper AND lower(p.name_paper) like '%труда%' AND NOT lower(p.name_paper) like '%кчр%')
                GROUP BY ppi.key_person
            ), persons AS (
                SELECT
                            p.key_person as id,
                            snils_format(p.snils_person) as snils,
                            p.surname_person as surname,
                            p.name_person as name,
                            p.patronymic_person as patronymic,
                            p.birthday_person as birthday,
                            pp.serial_person_paper as paperSeries,
                            pp.number_person_paper as paperNumber,
                            pp.issue_person_paper as paperIssuer,
                            pp.isue_date_person_paper as paperIssueDate
                    FROM person p
                    INNER JOIN personIds ON p.key_person = personIds.id
                    INNER JOIN person_paper pp ON pp.key_person_paper = personIds.personPaperId
                    WHERE COALESCE(p.surname_person, '') != '' AND COALESCE(p.name_person, '') != '' AND COALESCE(p.patronymic_person, '') != '' AND p.birthday_person IS NOT NULL
            )
            SELECT * FROM persons WHERE snils != ''""";
}
