package org.evomaster.dbconstraint;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.evomaster.dbconstraint.ast.*;
import org.evomaster.dbconstraint.parser.SqlConditionParserException;
import org.evomaster.dbconstraint.parser.jsql.JSqlConditionParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SqlConditionParserTest {

    private static SqlComparisonCondition lte(SqlCondition left, SqlCondition right) {
        return new SqlComparisonCondition(left, SqlComparisonOperator.LESS_THAN_OR_EQUAL, right);
    }

    private static SqlComparisonCondition eq(SqlCondition left, SqlCondition right) {
        return new SqlComparisonCondition(left, SqlComparisonOperator.EQUALS_TO, right);
    }

    private static SqlColumn column(String columnName) {
        return new SqlColumn(columnName);
    }

    private static SqlAndCondition and(SqlCondition left, SqlCondition right) {
        return new SqlAndCondition(left, right);
    }

    private static SqlBigIntegerLiteralValue intL(int i) {
        return new SqlBigIntegerLiteralValue(i);
    }

    private static SqlStringLiteralValue str(String literalValue) {
        return new SqlStringLiteralValue(literalValue);
    }

    private static SqlIsNotNullCondition isNotNull(SqlColumn sqlColumn) {
        return new SqlIsNotNullCondition(sqlColumn);
    }


    private static SqlInCondition in(String columName, String... stringLiteralValues) {
        List<SqlCondition> stringLiterals = Arrays.stream(stringLiteralValues).map(SqlConditionParserTest::str).collect(Collectors.toList());
        SqlConditionList stringLiteralList = new SqlConditionList(stringLiterals);
        return new SqlInCondition(column(columName), stringLiteralList);
    }

    private static SqlCondition parse(String conditionSqlStr, ConstraintDatabaseType databaseType) throws SqlConditionParserException {
        JSqlConditionParser parser = new JSqlConditionParser();
        return parser.parse(conditionSqlStr, databaseType);
    }

    private static SqlCondition parse(String conditionSqlStr) throws SqlConditionParserException {
        return parse(conditionSqlStr, ConstraintDatabaseType.H2);
    }

    private static SqlSimilarToCondition similarTo(SqlColumn columnName, SqlStringLiteralValue pattern) {
        return new SqlSimilarToCondition(columnName, pattern);
    }

    private static SqlIsNullCondition isNull(SqlColumn columnName) {
        return new SqlIsNullCondition(columnName);
    }

    private static SqlLikeCondition like(SqlColumn columnName, String patternStr) {
        return new SqlLikeCondition(columnName, new SqlStringLiteralValue(patternStr));
    }

    private static SqlOrCondition or(SqlCondition... conditions) {
        return new SqlOrCondition(conditions);
    }


    @Test
    void testMinorThanOrEquals() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max<=10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.LESS_THAN_OR_EQUAL, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }


    @Test
    void testMinorThan() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max<10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.LESS_THAN, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }

    @Test
    void testGreaterThanValue() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max>10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.GREATER_THAN, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }

    @Test
    void testMinorThanColumn() throws SqlConditionParserException {
        SqlCondition actual = parse("10<age_max".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlBigIntegerLiteralValue(10), SqlComparisonOperator.LESS_THAN, new SqlColumn("age_max".toUpperCase()));
        assertEquals(expected, actual);
    }

    @Test
    void testMinorThanValue() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max<10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.LESS_THAN, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }

    @Test
    void testGreaterThanEquals() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max>=10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.GREATER_THAN_OR_EQUAL, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }

    @Test
    void testMinorThanEqualsColumn() throws SqlConditionParserException {
        SqlCondition actual = parse("10<=age_max".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlBigIntegerLiteralValue(10), SqlComparisonOperator.LESS_THAN_OR_EQUAL, new SqlColumn("age_max".toUpperCase()));
        assertEquals(expected, actual);
    }

    @Test
    void testEquals() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max=10".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("age_max".toUpperCase()), SqlComparisonOperator.EQUALS_TO, new SqlBigIntegerLiteralValue(10));
        assertEquals(expected, actual);
    }

    @Test
    void testTableAndColumnName() throws SqlConditionParserException {
        SqlCondition actual = parse("users.age_max<=100".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlColumn("users".toUpperCase(), "age_max".toUpperCase()), SqlComparisonOperator.LESS_THAN_OR_EQUAL, new SqlBigIntegerLiteralValue(100));
        assertEquals(expected, actual);
    }

    @Test
    void testAndExpression() throws SqlConditionParserException {
        SqlCondition actual = parse("18<=age_max AND age_max<=100".toUpperCase());
        SqlCondition expected = and(
                lte(intL(18), column("age_max".toUpperCase())),
                lte(column("age_max".toUpperCase()), intL(100)));

        assertEquals(expected, actual);

    }

    @Test
    void testParenthesis() throws SqlConditionParserException {
        SqlCondition actual = parse("18<=age_max".toUpperCase());
        SqlCondition expected = new SqlComparisonCondition(new SqlBigIntegerLiteralValue(18), SqlComparisonOperator.LESS_THAN_OR_EQUAL, new SqlColumn("age_max".toUpperCase()));
        assertEquals(expected, actual);
    }

    @Test
    void testInCondition() throws SqlConditionParserException {
        SqlCondition actual = parse("(STATUS in ('a', 'b'))");
        SqlCondition expected = in("STATUS", "a", "b");
        assertEquals(expected, actual);

    }

    @Test
    void testSingleLike() throws SqlConditionParserException {
        SqlCondition actual = parse("(F_ID LIKE 'hi')");
        SqlLikeCondition expected = like(column("F_ID"), "hi");
        assertEquals(expected, actual);
    }


    @Test
    void testConditionEquals() throws SqlConditionParserException {
        SqlCondition actual = parse("((STATUS = 'b') = (P_AT IS NOT NULL))");

        SqlCondition expected = eq(
                eq(column("STATUS"), str("b")),
                isNotNull(column("P_AT")));
        assertEquals(expected, actual);
    }

    @Test
    void testMultiLike() throws SqlConditionParserException {
        SqlCondition actual = parse("(F_ID LIKE 'hi'\n" +
                "    OR F_ID LIKE '%foo%'\n" +
                "    OR F_ID LIKE '%foo%x%'\n" +
                "    OR F_ID LIKE '%bar%'\n" +
                "    OR F_ID LIKE '%bar%y%'\n" +
                "    OR F_ID LIKE '%hello%')");
        SqlOrCondition expected = or(or(or(or(or(like(column("F_ID"), "hi"),
                like(column("F_ID"), "%foo%")),
                like(column("F_ID"), "%foo%x%")),
                like(column("F_ID"), "%bar%")),
                like(column("F_ID"), "%bar%y%")),
                like(column("F_ID"), "%hello%")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testMultipleInCondition() throws SqlConditionParserException {
        SqlCondition actual = parse("(STATUS IN ('A', 'B', 'C', 'D', 'E'))");
        SqlInCondition expected = in("STATUS", "A", "B", "C", "D", "E");
        assertEquals(expected, actual);
    }

    @Disabled("SIMILAR TO is not directly supported by JSQL parser")
    @Test
    void testSimilarTo() throws SqlConditionParserException {
        SqlCondition actual = parse("(W_ID SIMILAR TO '/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?')");
        SqlSimilarToCondition expected = similarTo(column("W_ID"), str("/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?"));
        assertEquals(expected, actual);
    }

    @Test
    void testPostgresSimilarEscape() throws SqlConditionParserException {
        SqlCondition actual = parse("(w_id ~ similar_escape('/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?'::text, NULL::text))");
        SqlSimilarToCondition expected = similarTo(column("w_id"), str("/foo/__/bar/(left|right)/[0-9]{4}-[0-9]{2}-[0-9]{2}(/[0-9]*)?"));
        assertEquals(expected, actual);
    }

    @Test
    void testPostgresLike() throws SqlConditionParserException {
        SqlCondition actual = parse("((f_id ~~ 'hi'::text) OR (f_id ~~ '%foo%'::text) OR (f_id ~~ '%foo%x%'::text) OR (f_id ~~ '%bar%'::text) OR (f_id ~~ '%bar%y%'::text) OR (f_id ~~ '%hello%'::text))");
        SqlOrCondition expected = or(or(or(or(or(like(column("f_id"), "hi"),
                like(column("f_id"), "%foo%")),
                like(column("f_id"), "%foo%x%")),
                like(column("f_id"), "%bar%")),
                like(column("f_id"), "%bar%y%")),
                like(column("f_id"), "%hello%")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testMySQLLike() throws SqlConditionParserException {
        SqlCondition actual = parse("((f_id like 'hi') or (f_id like '%foo%') or (f_id like '%foo%x%') or (f_id like '%bar%') or (f_id like '%bar%y%') or (f_id like '%hello%'))");
        SqlOrCondition expected = or(or(or(or(or(like(column("f_id"), "hi"),
                like(column("f_id"), "%foo%")),
                like(column("f_id"), "%foo%x%")),
                like(column("f_id"), "%bar%")),
                like(column("f_id"), "%bar%y%")),
                like(column("f_id"), "%hello%")
        );
        assertEquals(expected, actual);
    }



    @Test
    void testIsNull() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max IS NULL".toUpperCase());
        SqlIsNullCondition expected = isNull(column("AGE_MAX"));
        assertEquals(expected, actual);
    }


    @Test
    void testIsNotNull() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max IS NOT NULL".toUpperCase());
        SqlIsNotNullCondition expected = isNotNull(column("AGE_MAX"));
        assertEquals(expected, actual);
    }

    @Test
    void testEqualsNull() throws SqlConditionParserException {
        SqlCondition actual = parse("age_max = NULL".toUpperCase());
        SqlComparisonCondition expected = eq(column("AGE_MAX"), new SqlNullLiteralValue());
        assertEquals(expected, actual);
    }

    @Test
    void testBrokenExpression() {
        try {
            SqlCondition e = parse("age_max === NULL".toUpperCase());
            fail();
        } catch (SqlConditionParserException e) {
            // do nothing
        }
    }

    @Test
    void testSingleLikeConstraint() throws SqlConditionParserException {
        SqlCondition actual = parse( "(\"F_ID\" IN(CAST('hi' AS CHARACTER LARGE OBJECT(2)), CAST('low' AS CHARACTER LARGE OBJECT(3))))");
        SqlCondition expected = in("F_ID", "hi", "low");
        assertEquals(expected, actual);

    }


    @Disabled
    @Test
    void testPerformanceIssueInFamilieBaSak() throws Exception{

        /*
            These constraints come from familie-ba-sak SUT.
            they crash, and can take several minutes to investigate.

            For crash, reported issue on parser library:

            https://github.com/JSQLParser/JSqlParser/issues/1997
         */

        //String sql = "((begrunnelse)::text = ANY ((ARRAY['INNVILGET_PRIMÆRLAND_UK_STANDARD'::character varying, 'INNVILGET_PRIMÆRLAND_BARNET_BOR_I_NORGE'::character varying, 'INNVILGET_PRIMÆRLAND_BARNETRYGD_ALLEREDE_UTBETALT'::character varying, 'INNVILGET_PRIMÆRLAND_UK_BARNETRYGD_ALLEREDEUTBETALT'::character varying, 'INNVILGET_PRIMÆRLAND_BEGGE_FORELDRE_BOSATT_I_NORGE'::character varying, 'INNVILGET_PRIMÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'INNVILGET_PRIMÆRLAND_SÆRKULLSBARN_ANDRE_BARN_OVERTATT_ANSVAR'::character varying, 'INNVILGET_PRIMÆRLAND_UK_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'INNVILGET_PRIMÆRLAND_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'INNVILGET_PRIMÆRLAND_STANDARD'::character varying, 'INNVILGET_PRIMÆRLAND_UK_ALENEANSVAR'::character varying, 'INNVILGET_TILLEGGSBEGRUNNELSE_UTBETALING_TIL_ANNEN_FORELDER'::character varying, 'INNVILGET_PRIMÆRLAND_BARNET_FLYTTET_TIL_NORGE'::character varying, 'INNVILGET_PRIMÆRLAND_BEGGE_FORELDRE_JOBBER_I_NORGE'::character varying, 'INNVILGET_PRIMÆRLAND_TO_ARBEIDSLAND_ANNET_LAND_UTBETALER'::character varying, 'INNVILGET_PRIMÆRLAND_SÆRKULLSBARN_ANDRE_BARN'::character varying, 'INNVILGET_PRIMÆRLAND_UK_TO_ARBEIDSLAND_ANNET_LAND_UTBETALER'::character varying, 'INNVILGET_PRIMÆRLAND_ALENEANSVAR'::character varying, 'INNVILGET_SEKUNDÆRLAND_STANDARD'::character varying, 'INNVILGET_SEKUNDÆRLAND_ALENEANSVAR'::character varying, 'INNVILGET_TILLEGGSTEKST_NULLUTBETALING'::character varying, 'INNVILGET_SEKUNDÆRLAND_UK_STANDARD'::character varying, 'INNVILGET_SEKUNDÆRLAND_UK_ALENEANSVAR'::character varying, 'INNVILGET_SEKUNDÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'INNVILGET_SEKUNDÆRLAND_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'INNVILGET_SEKUNDÆRLAND_UK_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'INNVILGET_TILLEGGSTEKST_SATSENDRING'::character varying, 'INNVILGET_TILLEGGSTEKST_VALUTAJUSTERING'::character varying, 'INNVILGET_TILLEGGSTEKST_SATSENDRING_OG_VALUTAJUSTERING'::character varying, 'INNVILGET_TILLEGGSTEKST_SEKUNDÆR_DELT_BOSTED_ANNEN_FORELDER_IKKE_SØKT'::character varying, 'INNVILGET_PRIMÆRLAND_TILLEGGSTEKST_VEDTAK_FØR_SED'::character varying, 'INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_FÅR_YTELSE_I_UTLANDET'::character varying, 'INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_FÅR_YTELSE_I_UTLANDET'::character varying, 'INNVILGET_SEKUNDÆRLAND_BEGGE_FORELDRE_BOSATT_I_NORGE'::character varying, 'INNVILGET_TILLEGGSTEKST_PRIMÆR_DELT_BOSTED_ANNEN_FORELDER_IKKE_RETT'::character varying, 'INNVILGET_TILLEGGSTEKST_SEKUNDÆR_FULL_UTBETALING'::character varying, 'INNVILGET_TILLEGGSTEKST_SEKUNDÆR_AVTALE_DELT_BOSTED'::character varying, 'INNVILGET_TILLEGGSTEKST_SEKUNDÆR_DELT_BOSTED_ANNEN_FORELDER_IKKE_RETT'::character varying, 'INNVILGET_GYLDIG_KONTONUMMER_REGISTRERT_EØS'::character varying, 'INNVILGET_TILLEGGSTEKST_SEKUNDÆR_IKKE_FÅTT_SVAR_PÅ_SED'::character varying, 'INNVILGET_TILLEGGESTEKST_UK_FULL_ETTERBETALING'::character varying, 'INNVILGET_PRIMÆRLAND_DEN_ANDRE_FORELDEREN_UTSENDT_ARBEIDSTAKER'::character varying, 'INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_STANDARD'::character varying, 'INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_UK_STANDARD'::character varying, 'INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_UK_OG_STANDARD'::character varying, 'INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_UTSENDT_ARBEIDSTAKER'::character varying, 'INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_STANDARD'::character varying, 'INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_UK_STANDARD'::character varying, 'INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'OPPHØR_EØS_STANDARD'::character varying, 'OPPHØR_EØS_SØKER_BER_OM_OPPHØR'::character varying, 'OPPHØR_BARN_BOR_IKKE_I_EØS_LAND'::character varying, 'OPPHØR_IKKE_STATSBORGER_I_EØS_LAND'::character varying, 'OPPHØR_SENTRUM_FOR_LIVSINTERESSE'::character varying, 'OPPHØR_IKKE_ANSVAR_FOR_BARN'::character varying, 'OPPHØR_IKKE_OPPHOLDSRETT_SOM_FAMILIEMEDLEM'::character varying, 'OPPHØR_SEPARASJONSAVTALEN_GJELDER_IKKE'::character varying, 'OPPHØR_SØKER_OG_BARN_BOR_IKKE_I_EØS_LAND'::character varying, 'OPPHØR_SØKER_BOR_IKKE_I_EØS_LAND'::character varying, 'OPPHØR_ARBEIDER_MER_ENN_25_PROSENT_I_ANNET_EØS_LAND'::character varying, 'OPPHØR_UTSENDT_ARBEIDSTAKER_FRA_EØS_LAND'::character varying, 'OPPHOR_UGYLDIG_KONTONUMMER_EØS'::character varying, 'OPPHOR_ETT_BARN_DØD_EØS'::character varying, 'OPPHOR_FLERE_BARN_DØDE_EØS'::character varying, 'OPPHØR_SELVSTENDIG_RETT_OPPHØR'::character varying, 'OPPHØR_SELVSTENDIG_RETT_UTSENDT_ARBEIDSTAKER_FRA_ANNET_EØS_LAND'::character varying, 'AVSLAG_EØS_IKKE_EØS_BORGER'::character varying, 'AVSLAG_EØS_IKKE_BOSATT_I_EØS_LAND'::character varying, 'AVSLAG_EØS_JOBBER_IKKE'::character varying, 'AVSLAG_EØS_UTSENDT_ARBEIDSTAKER_FRA_ANNET_EØS_LAND'::character varying, 'AVSLAG_EØS_ARBEIDER_MER_ENN_25_PROSENT_I_ANNET_EØS_LAND'::character varying, 'AVSLAG_EØS_KUN_KORTE_USAMMENHENGENDE_ARBEIDSPERIODER'::character varying, 'AVSLAG_EØS_IKKE_PENGER_FRA_NAV_SOM_ERSTATTER_LØNN'::character varying, 'AVSLAG_EØS_SEPARASJONSAVTALEN_GJELDER_IKKE'::character varying, 'AVSLAG_EØS_IKKE_LOVLIG_OPPHOLD_SOM_EØS_BORGER'::character varying, 'AVSLAG_EØS_IKKE_OPPHOLDSRETT_SOM_FAMILIEMEDLEM_AV_EØS_BORGER'::character varying, 'AVSLAG_EØS_IKKE_STUDENT'::character varying, 'AVSLAG_EØS_IKKE_ANSVAR_FOR_BARN'::character varying, 'AVSLAG_EØS_VURDERING_IKKE_ANSVAR_FOR_BARN'::character varying, 'AVSLAG_FAAR_DAGPENGER_FRA_ANNET_EOS_LAND'::character varying, 'AVSLAG_SELVSTENDIG_NAERINGSDRIVENDE_NORGE_ARBEIDSTAKER_I_ANNET_EOS_LAND'::character varying, 'AVSLAG_EØS_UREGISTRERT_BARN'::character varying, 'AVSLAG_SELVSTENDIG_RETT_STANDARD_AVSLAG'::character varying, 'AVSLAG_SELVSTENDIG_RETT_UTSENDT_ARBEIDSTAKER_FRA_ANNET_EØS_LAND'::character varying, 'AVSLAG_SELVSTENDIG_RETT_BOR_IKKE_FAST_MED_BARNET'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_STANDARD'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_ALENEANSVAR'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_BEGGE_FORELDRE_BOSATT_I_NORGE'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_BEGGE_FORELDRE_JOBBER_I_NORGE'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_UK_STANDARD'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_UK_ALENEANSVAR'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_BARNET_BOR_I_NORGE'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_SÆRKULLSBARN_ANDRE_BARN_OVERTATT_ANSVAR'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_TO_ARBEIDSLAND_ANNET_LAND_UTBETALER'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_UK_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'FORTSATT_INNVILGET_PRIMÆRLAND_UK_TO_ARBEIDSLAND_ANNET_LAND_UTBETALER'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_FÅR_YTELSE_I_UTLANDET'::character varying, 'FORTSATT_INNVILGET_TILLEGGSBEGRUNNELSE_UTBETALING_TIL_ANNEN_FORELDER'::character varying, 'FORTSETT_INNVILGET_PRIMÆRLAND_TILLEGGSTEKST_VEDTAK_FØR_SED'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_STANDARD'::character varying, 'FORTSETT_INNVILGET_TILLEGGSTEKST_NULLUTBETALING'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_ALENEANSVAR'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_UK_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_FÅR_YTELSE_I_UTLANDET'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_UK_ALENEANSVAR'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'FORTSETT_INNVILGET_SEKUNDÆRLAND_UK_TO_ARBEIDSLAND_NORGE_UTBETALER'::character varying, 'FORTSATT_INNVILGET_SEKUNDÆRLAND_BEGGE_FORELDRE_BOSATT_I_NORGE'::character varying, 'FORTSATT_INNVILGET_TILLEGGSTEKST_SEKUNDÆR_FULL_UTBETALING'::character varying, 'FORTSATT_INNVILGET_TILLEGGSTEKST_SEKUNDÆR_IKKE_FÅTT_SVAR_PÅ_SED'::character varying, 'FORTSATT_INNVILGET_TILLEGSTEKST_UK_FULL_UTBETALING'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_UK_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_PRIMÆRLAND_UK_OG_UTLAND_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_SEKUNDÆRLAND_UK_STANDARD'::character varying, 'FORTSATT_INNVILGET_SELVSTENDIG_RETT_SEKUNDAERLAND_UK_OG_UTLAND_STANDARD'::character varying, 'REDUKSJON_BARN_DØD_EØS'::character varying, 'REDUKSJON_SØKER_BER_OM_OPPHØR_EØS'::character varying, 'REDUKSJON_BARN_BOR_IKKE_I_EØS'::character varying, 'REDUKSJON_IKKE_ANSVAR_FOR_BARN'::character varying])::text[]))";
        //String sql = "((vedtak_begrunnelse_spesifikasjon)::text = ANY ((ARRAY['INNVILGET_BOSATT_I_RIKTET'::character varying, 'INNVILGET_BOSATT_I_RIKTET_LOVLIG_OPPHOLD'::character varying, 'INNVILGET_LOVLIG_OPPHOLD_OPPHOLDSTILLATELSE'::character varying, 'INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER'::character varying, 'INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER_SKJØNNSMESSIG_VURDERING'::character varying, 'INNVILGET_LOVLIG_OPPHOLD_SKJØNNSMESSIG_VURDERING_TREDJELANDSBORGER'::character varying, 'INNVILGET_LOVLIG_OPPHOLD_SKJØNNSMESSIG_VURDERING_TREDJELANDSBORGER_SØKER'::character varying, 'INNVILGET_TREDJELANDSBORGER_LOVLIG_OPPHOLD_FOR_BOSATT_I_NORGE'::character varying, 'INNVILGET_OMSORG_FOR_BARN'::character varying, 'INNVILGET_BOR_HOS_SØKER'::character varying, 'INNVILGET_BOR_HOS_SØKER_SKJØNNSMESSIG'::character varying, 'INNVILGET_FAST_OMSORG_FOR_BARN'::character varying, 'INNVILGET_NYFØDT_BARN_FØRSTE'::character varying, 'INNVILGET_NYFØDT_BARN'::character varying, 'INNVILGET_FØDSELSHENDELSE_NYFØDT_BARN_FØRSTE'::character varying, 'INNVILGET_FØDSELSHENDELSE_NYFØDT_BARN'::character varying, 'INNVILGET_MEDLEM_I_FOLKETRYGDEN'::character varying, 'INNVILGET_BARN_BOR_SAMMEN_MED_MOTTAKER'::character varying, 'INNVILGET_BEREDSKAPSHJEM'::character varying, 'INNVILGET_HELE_FAMILIEN_TRYGDEAVTALE'::character varying, 'INNVILGET_HELE_FAMILIEN_PLIKTIG_MEDLEM'::character varying, 'INNVILGET_SØKER_OG_BARN_PLIKTIG_MEDLEM'::character varying, 'INNVILGET_ENIGHET_OM_OPPHØR_AV_AVTALE_OM_DELT_BOSTED'::character varying, 'INNVILGET_VURDERING_HELE_FAMILIEN_FRIVILLIG_MEDLEM'::character varying, 'INNVILGET_UENIGHET_OM_OPPHØR_AV_AVTALE_OM_DELT_BOSTED'::character varying, 'INNVILGET_HELE_FAMILIEN_FRIVILLIG_MEDLEM'::character varying, 'INNVILGET_VURDERING_HELE_FAMILIEN_PLIKTIG_MEDLEM'::character varying, 'INNVILGET_SØKER_OG_BARN_OPPHOLD_I_UTLANDET_IKKE_MER_ENN_3_MÅNEDER'::character varying, 'INNVILGET_OPPHOLD_I_UTLANDET_IKKE_MER_ENN_3_MÅNEDER'::character varying, 'INNVILGET_SØKER_OG_BARN_FRIVILLIG_MEDLEM'::character varying, 'INNVILGET_VURDERING_SØKER_OG_BARN_FRIVILLIG_MEDLEM'::character varying, 'INNVILGET_ETTERBETALING_3_ÅR'::character varying, 'INNVILGET_SØKER_OG_BARN_TRYGDEAVTALE'::character varying, 'INNVILGET_ALENE_FRA_FØDSEL'::character varying, 'INNVILGET_VURDERING_SØKER_OG_BARN_PLIKTIG_MEDLEM'::character varying, 'INNVILGET_BARN_OPPHOLD_I_UTLANDET_IKKE_MER_ENN_3_MÅNEDER'::character varying, 'INNVILGET_SATSENDRING'::character varying, 'INNVILGET_FLYTTET_ETTER_SEPARASJON'::character varying, 'INNVILGET_SEPARERT'::character varying, 'INNVILGET_VARETEKTSFENGSEL_SAMBOER'::character varying, 'INNVILGET_AVTALE_DELT_BOSTED_FÅR_FRA_FLYTTETIDSPUNKT'::character varying, 'INNVILGET_TVUNGENT_PSYKISK_HELSEVERN_GIFT'::character varying, 'INNVILGET_TVUNGENT_PSYKISK_HELSEVERN_SAMBOER'::character varying, 'INNVILGET_FENGSEL_GIFT'::character varying, 'INNVILGET_VURDERING_EGEN_HUSHOLDNING'::character varying, 'INNVILGET_FORSVUNNET_SAMBOER'::character varying, 'INNVILGET_AVTALE_DELT_BOSTED_FÅR_FRA_AVTALETIDSPUNKT'::character varying, 'INNVILGET_FORVARING_GIFT'::character varying, 'INNVILGET_MEKLINGSATTEST_OG_VURDERING_EGEN_HUSHOLDNING'::character varying, 'INNVILGET_FENGSEL_SAMBOER'::character varying, 'INNVILGET_FLYTTING_ETTER_MEKLINGSATTEST'::character varying, 'INNVILGET_FORVARING_SAMBOER'::character varying, 'INNVILGET_SEPARERT_OG_VURDERING_EGEN_HUSHOLDNING'::character varying, 'INNVILGET_BARN_16_ÅR'::character varying, 'INNVILGET_SAMBOER_DØD'::character varying, 'INNVILGET_MEKLINGSATTEST'::character varying, 'INNVILGET_FLYTTET_ETTER_SKILT'::character varying, 'INNVILGET_ENSLIG_MINDREÅRIG_FLYKTNING'::character varying, 'INNVILGET_VARETEKTSFENGSEL_GIFT'::character varying, 'INNVILGET_SAMBOER_UTEN_FELLES_BARN_OG_VURDERING_EGEN_HUSHOLDNING'::character varying, 'INNVILGET_FORSVUNNET_EKTEFELLE'::character varying, 'INNVILGET_FAKTISK_SEPARASJON'::character varying, 'INNVILGET_SAMBOER_UTEN_FELLES_BARN'::character varying, 'INNVILGET_VURDERING_AVTALE_DELT_BOSTED_FØLGES'::character varying, 'INNVILGET_SKILT_OG_VURDERING_EGEN_HUSHOLDNING'::character varying, 'INNVILGET_BOR_ALENE_MED_BARN'::character varying, 'INNVILGET_SKILT'::character varying, 'INNVILGET_RETTSAVGJØRELSE_DELT_BOSTED'::character varying, 'INNVILGET_EKTEFELLE_DØD'::character varying, 'INNVILGET_SMÅBARNSTILLEGG'::character varying, 'INNVILGET_ANNEN_FORELDER_IKKE_SØKT_DELT_BARNETRYGD_ENKELTBARN'::character varying, 'INNVILGET_ANNEN_FORELDER_IKKE_SØKT_DELT_BARNETRYGD_ALLE_BARNA'::character varying, 'INNVILGET_TILLEGGSTEKST_SAMSBOER_12_AV_SISTE_18'::character varying, 'INNVILGET_ERKLÆRING_OM_MOTREGNING'::character varying, 'INNVILGET_TILLEGGSTEKST_TRANSPORTERKLÆRING_HELE_ETTERBETALINGEN'::character varying, 'INNVILGET_TILLEGGSTEKST_TRANSPORTERKLÆRING_DELER_AV_ETTERBETALINGEN'::character varying, 'INNVILGET_EØS_BORGER_JOBBER'::character varying, 'INNVILGET_EØS_BORGER_UTBETALING_FRA_NAV'::character varying, 'INNVILGET_EØS_BORGER_EKTEFELLE_JOBBER'::character varying, 'INNVILGET_EØS_BORGER_SAMBOER_JOBBER'::character varying, 'INNVILGET_EØS_BORGER_EKTEFELLE_UTBETALING_FRA_NAV'::character varying, 'INNVILGET_EØS_BORGER_SAMBOER_UTBETALING_FRA_NAV'::character varying, 'INNVILGET_FAKTISK_SEPARASJON_SEPARERT_ETTERPÅ'::character varying, 'INNVILGET_BARN_16ÅR_UTVIDET_FRA_FLYTTING'::character varying, 'INNVILGET_TILLEGGSTEKST_OPPHØR_UTVIDET_NYFØDT_BARN'::character varying, 'INNVILGET_TILLEGGSTEKST_SAMBOER_UNDER_12_MÅNEDER_FØR_GIFT'::character varying, 'INNVILGET_TILLEGGSTEKST_SAMBOER_UNDER_12_MÅNEDER_FØR_NYTT_BARN'::character varying, 'INNVILGET_TILLEGGSTEKST_SAMBOER_UNDER_12_MÅNEDER'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_JOBBER'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_UTBETALING_NAV'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_EKTEFELLE_JOBBER'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_SAMBOER_JOBBER'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_EKTEFELLE_UTBETALING_NAV'::character varying, 'INNVILGET_TILLEGGSTEKST_EØS_BORGER_SAMBOER_UTBETALING_NAV'::character varying, 'INNVILGET_TILLEGGSTEKST_TREDJELANDSBORGER_OPPHOLDSTILLATELSE'::character varying, 'INNVILGET_TILLEGGSTEKST_TREDJELANDSBORGER_OPPHOLDSTILLATELSE_SØKER'::character varying, 'INNVILGET_MEDLEM_AV_FOLKETRYGDEN_UTEN_DATO'::character varying, 'INNVILGET_GYLDIG_KONTONUMMER_REGISTRERT'::character varying, 'INNVILGET_FULL_UTBETALING_AVTALE_DELT_BOSTED_ANNEN_OMSORGSPERSON'::character varying, 'INNVILGET_FULL_UTBETALING_ANNEN_FORELDER_ØNSKER_IKKE_DELT_BARNETRYGD'::character varying, 'INNVILGET_OVERGANG_EØS_TIL_NASJONAL_NORSK_NORDISK_FAMILIE'::character varying, 'INNVILGET_OVERGANG_EØS_TIL_NASJONAL_SEPARASJONSAVTALEN'::character varying, 'INNVILGET_FÅR_ETTERBETALT_UTVIDET_FOR_PRAKTISERT_DELT_BOSTED'::character varying, 'INNVILGET_DATO_SKRIFTLIG_AVTALE_DELT_BOSTED'::character varying, 'INNVILGET_DELT_FRA_SKRIFTLIG_AVTALE_HAR_SØKT_FOR_PRAKTISERT_DELT_BOSTED'::character varying, 'INNVILGET_OPPHOLD_PAA_SVALBARD'::character varying, 'REDUKSJON_BOSATT_I_RIKTET'::character varying, 'REDUKSJON_LOVLIG_OPPHOLD_OPPHOLDSTILLATELSE_BARN'::character varying, 'REDUKSJON_FLYTTET_BARN'::character varying, 'REDUKSJON_BARN_DØD'::character varying, 'REDUKSJON_FAST_OMSORG_FOR_BARN'::character varying, 'REDUKSJON_UNDER_18_ÅR'::character varying, 'REDUKSJON_UNDER_6_ÅR'::character varying, 'REDUKSJON_UNDER_18_ÅR_AUTOVEDTAK'::character varying, 'REDUKSJON_UNDER_6_ÅR_AUTOVEDTAK'::character varying, 'REDUKSJON_DELT_BOSTED_ENIGHET'::character varying, 'REDUKSJON_DELT_BOSTED_UENIGHET'::character varying, 'REDUKSJON_ENDRET_MOTTAKER'::character varying, 'REDUKSJON_ANNEN_FORELDER_IKKE_LENGER_FRIVILLIG_MEDLEM'::character varying, 'REDUKSJON_ANNEN_FORELDER_IKKE_MEDLEM'::character varying, 'REDUKSJON_ANNEN_FORELDER_IKKE_LENGER_MEDLEM_TRYGDEAVTALE'::character varying, 'REDUKSJON_ANNEN_FORELDER_IKKE_LENGER_PLIKTIG_MEDLEM'::character varying, 'REDUKSJON_VURDERING_BARN_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_ÅRENE_'::character varying, 'REDUKSJON_VURDERING_BARN_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_TO_ÅR_'::character varying, 'REDUKSJON_SATSENDRING'::character varying, 'REDUKSJON_NYFØDT_BARN'::character varying, 'REDUKSJON_VURDERING_SØKER_GIFTET_SEG'::character varying, 'REDUKSJON_VURDERING_SAMBOER_MER_ENN_12_MÅNEDER'::character varying, 'REDUKSJON_AVTALE_FAST_BOSTED'::character varying, 'REDUKSJON_EKTEFELLE_IKKE_I_FENGSEL'::character varying, 'REDUKSJON_SAMBOER_MER_ENN_12_MÅNEDER'::character varying, 'REDUKSJON_SAMBOER_IKKE_I_TVUNGENT_PSYKISK_HELSEVERN'::character varying, 'REDUKSJON_SAMBOER_IKKE_EGEN_HUSHOLDNING'::character varying, 'REDUKSJON_SAMBOER_IKKE_I_FENGSEL'::character varying, 'REDUKSJON_VURDERING_FLYTTET_SAMMEN_MED_EKTEFELLE'::character varying, 'REDUKSJON_VURDERING_FORELDRENE_BOR_SAMMEN'::character varying, 'REDUKSJON_SAMBOER_IKKE_I_FORVARING'::character varying, 'REDUKSJON_EKTEFELLE_IKKE_I_TVUNGENT_PSYKISK_HELSEVERN'::character varying, 'REDUKSJON_EKTEFELLE_IKKE_I_FORVARING'::character varying, 'REDUKSJON_FORELDRENE_BOR_SAMMEN'::character varying, 'REDUKSJON_EKTEFELLE_IKKE_LENGER_FORSVUNNET'::character varying, 'REDUKSJON_RETTSAVGJØRELSE_FAST_BOSTED'::character varying, 'REDUKSJON_FLYTTET_SAMMEN_MED_ANNEN_FORELDER'::character varying, 'REDUKSJON_GIFT_IKKE_EGEN_HUSHOLDNING'::character varying, 'REDUKSJON_FLYTTET_SAMMEN_MED_EKTEFELLE'::character varying, 'REDUKSJON_IKKE_AVTALE_DELT_BOSTED'::character varying, 'REDUKSJON_SØKER_GIFTER_SEG'::character varying, 'REDUKSJON_SAMBOER_IKKE_LENGER_FORSVUNNET'::character varying, 'REDUKSJON_VURDERING_FLYTTET_SAMMEN_MED_ANNEN_FORELDER'::character varying, 'REDUKSJON_SMÅBARNSTILLEGG_IKKE_LENGER_BARN_UNDER_TRE_ÅR'::character varying, 'REDUKSJON_SMÅBARNSTILLEGG_IKKE_LENGER_FULL_OVERGANGSSTØNAD'::character varying, 'REDUKSJON_DELT_BARNETRYGD_ANNEN_FORELDER_SØKT'::character varying, 'REDUKSJON_DELT_BARNETRYGD_HASTEVEDTAK'::character varying, 'REDUKSJON_IKKE_BOSATT_I_NORGE'::character varying, 'REDUKSJON_BARN_BOR_IKKE_MED_SØKER'::character varying, 'REDUKSJON_IKKE_OPPHOLDSTILLATELSE'::character varying, 'REDUKSJON_AVTALE_DELT_BOSTED_IKKE_GYLDIG'::character varying, 'REDUKSJON_AVTALE_DELT_BOSTED_FØLGES_IKKE'::character varying, 'REDUKSJON_FORELDRENE_BODDE_SAMMEN'::character varying, 'REDUKSJON_VURDERING_FORELDRENE_BODDE_SAMMEN'::character varying, 'REDUKSJON_VAR_IKKE_MEDLEM'::character varying, 'REDUKSJON_VURDERING_VAR_IKKE_MEDLEM'::character varying, 'REDUKSJON_ANDRE_FORELDER_VAR_IKKE_MEDLEM'::character varying, 'REDUKSJON_VURDERING_ANDRE_FORELDER_VAR_IKKE_MEDLEM'::character varying, 'REDUKSJON_DELT_BOSTED_GENERELL'::character varying, 'REDUKSJON_BARN_DØDE_SAMME_MÅNED_SOM_FØDT'::character varying, 'REDUKSJON_MANGLER_MEKLINGSATTEST'::character varying, 'REDUKSJON_FORELDRENE_BOR_SAMMEN_ANNEN_FORELDER_SØKT'::character varying, 'REDUKSJON_SØKER_BER_OM_OPPHØR'::character varying, 'SMÅBARNSTILLEGG_HADDE_IKKE_FULL_OVERGANGSSTØNAD'::character varying, 'REDUKSJON_BARN_MED_SAMBOER_FØR_BODD_SAMMEN_12_MND'::character varying, 'REDUKSJON_SMÅBARNSTILLEGG_HAR_IKKE_UTVIDET_BARNETRYGD'::character varying, 'REDUKSJON_SØKER_ER_GIFT'::character varying, 'REDUKSJON_SØKER_BER_OM_OPPHØR_UTVIDET'::character varying, 'REDUKSJON_DELT_BOSTED_SØKER_BER_OM_OPPHØR'::character varying, 'REDUKSJON_FAST_BOSTED_AVTALE'::character varying, 'REDUKSJON_BEGGE_FORELDRE_FÅTT_BARNETRYGD'::character varying, 'REDUKSJON_BARN_BOR_I_INSTITUSJON'::character varying, 'AVSLAG_BOSATT_I_RIKET'::character varying, 'AVSLAG_LOVLIG_OPPHOLD_TREDJELANDSBORGER'::character varying, 'AVSLAG_BOR_HOS_SØKER'::character varying, 'AVSLAG_OMSORG_FOR_BARN'::character varying, 'AVSLAG_LOVLIG_OPPHOLD_EØS_BORGER'::character varying, 'AVSLAG_LOVLIG_OPPHOLD_SKJØNNSMESSIG_VURDERING_TREDJELANDSBORGER'::character varying, 'AVSLAG_MEDLEM_I_FOLKETRYGDEN'::character varying, 'AVSLAG_FORELDRENE_BOR_SAMMEN'::character varying, 'AVSLAG_UNDER_18_ÅR'::character varying, 'AVSLAG_UGYLDIG_AVTALE_OM_DELT_BOSTED'::character varying, 'AVSLAG_IKKE_AVTALE_OM_DELT_BOSTED'::character varying, 'AVSLAG_SÆRKULLSBARN'::character varying, 'AVSLAG_UREGISTRERT_BARN'::character varying, 'AVSLAG_IKKE_DOKUMENTERT_BOSATT_I_NORGE'::character varying, 'AVSLAG_IKKE_MEDLEM'::character varying, 'AVSLAG_VURDERING_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_ÅRENE'::character varying, 'AVSLAG_VURDERING_ANNEN_FORELDER_IKKE_MEDLEM'::character varying, 'AVSLAG_IKKE_FRIVILLIG_MEDLEM'::character varying, 'AVSLAG_IKKE_PLIKTIG_MEDLEM'::character varying, 'AVSLAG_ANNEN_FORELDER_IKKE_MEDLEM_ETTER_TRYGDEAVTALE'::character varying, 'AVSLAG_ANNEN_FORELDER_IKKE_PLIKTIG_MEDLEM'::character varying, 'AVSLAG_VURDERING_IKKE_MEDLEM'::character varying, 'AVSLAG_ANNEN_FORELDER_IKKE_FRIVILLIG_MEDLEM'::character varying, 'AVSLAG_IKKE_MEDLEM_ETTER_TRYGDEAVTALE'::character varying, 'AVSLAG_VURDERING_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_TO_ÅR'::character varying, 'AVSLAG_SAMBOER'::character varying, 'AVSLAG_SAMBOER_IKKE_FLYTTET_FRA_HVERANDRE'::character varying, 'AVSLAG_BARN_HAR_FAST_BOSTED'::character varying, 'AVSLAG_IKKE_EGEN_HUSHOLDNING_SAMBOER'::character varying, 'AVSLAG_GIFT_MIDLERTIDIG_ADSKILLELSE'::character varying, 'AVSLAG_IKKE_EGEN_HUSHOLDNING_GIFT'::character varying, 'AVSLAG_MANGLER_AVTALE_DELT_BOSTED'::character varying, 'AVSLAG_VURDERING_IKKE_FLYTTET_FRA_EKTEFELLE'::character varying, 'AVSLAG_RETTSAVGJØRELSE_SAMVÆR'::character varying, 'AVSLAG_IKKE_SEPARERT'::character varying, 'AVSLAG_FENGSEL_UNDER_6_MÅNEDER_EKTEFELLE'::character varying, 'AVSLAG_IKKE_DOKUMENTERT_SKILT'::character varying, 'AVSLAG_VURDERING_IKKE_MEKLINGSATTEST'::character varying, 'AVSLAG_FORVARING_UNDER_6_MÅNEDER_EKTEFELLE'::character varying, 'AVSLAG_EKTEFELLE_FORSVUNNET_MINDRE_ENN_6_MÅNEDER'::character varying, 'AVSLAG_VURDERING_FORELDRENE_BOR_SAMMEN'::character varying, 'AVSLAG_SAMBOER_MIDLERTIDIG_ADSKILLELSE'::character varying, 'AVSLAG_IKKE_FLYTTET_FRA_EKTEFELLE'::character varying, 'AVSLAG_IKKE_MEKLINGSATTEST'::character varying, 'AVSLAG_FORVARING_UNDER_6_MÅNEDER_SAMBOER'::character varying, 'AVSLAG_IKKE_DOKUMENTERT_EKTEFELLE_DØD'::character varying, 'AVSLAG_VURDERING_IKKE_TVUNGENT_PSYKISK_HELSEVERN_EKTEFELLE'::character varying, 'AVSLAG_VURDERING_IKKE_SEPARERT'::character varying, 'AVSLAG_GIFT'::character varying, 'AVSLAG_SAMBOER_FORSVUNNET_MINDRE_ENN_6_MÅNEDER'::character varying, 'AVSLAG_VURDERING_IKKE_TVUNGENT_PSYKISK_HELSEVERN_SAMBOER'::character varying, 'AVSLAG_VURDERING_SAMBOER_IKKE_FLYTTET_FRA_HVERANDRE'::character varying, 'AVSLAG_ENSLIG_MINDREÅRIG_FLYKTNING'::character varying, 'AVSLAG_IKKE_DELT_FORELDRENE_BOR_SAMMEN'::character varying, 'AVSLAG_IKKE_GYLDIG_AVTALE_DELT_BOSTED'::character varying, 'AVSLAG_FENGSEL_UNDER_6_MÅNEDER_SAMBOER'::character varying, 'AVSLAG_IKKE_DOKUMENTERT_SAMBOER_DØD'::character varying, 'AVSLAG_VURDERING_BOSATT_UNDER_12_MÅNEDER'::character varying, 'AVSLAG_IKKE_FLYTTET_FRA_TIDLIGERE_EKTEFELLE'::character varying, 'AVSLAG_VURDERING_IKKE_FLYTTET_FRA_TIDLIGERE_EKTEFELLE'::character varying, 'AVSLAG_AVTALE_OM_DELT_BOSTED_FØLGES_FORTSATT'::character varying, 'AVSLAG_IKKE_OPPHOLDSTILLATELSE_MER_ENN_12_MÅNEDER'::character varying, 'AVSLAG_BOR_IKKE_FAST_MED_BARNET'::character varying, 'AVSLAG_ENSLIG_MINDREÅRIG_FLYKTNING_BOR_I_INSTITUSJON'::character varying, 'OPPHØR_BARN_FLYTTET_FRA_SØKER'::character varying, 'OPPHØR_UTVANDRET'::character varying, 'OPPHØR_BARN_DØD'::character varying, 'OPPHØR_FLERE_BARN_DØD'::character varying, 'OPPHØR_SØKER_HAR_IKKE_FAST_OMSORG'::character varying, 'OPPHØR_HAR_IKKE_OPPHOLDSTILLATELSE'::character varying, 'OPPHØR_DELT_BOSTED_OPPHØRT_ENIGHET'::character varying, 'OPPHØR_DELT_BOSTED_OPPHØRT_UENIGHET'::character varying, 'OPPHØR_UNDER_18_ÅR'::character varying, 'OPPHØR_ENDRET_MOTTAKER'::character varying, 'OPPHØR_ANNEN_FORELDER_IKKE_LENGER_PLIKTIG_MEDLEM'::character varying, 'OPPHØR_SØKER_OG_BARN_IKKE_LENGER_PLIKTIG_MEDLEM'::character varying, 'OPPHØR_BOSATT_I_NORGE_UNNTATT_MEDLEMSKAP'::character varying, 'OPPHØR_ANNEN_FORELDER_IKKE_LENGER_MEDLEM_TRYGDEAVTALE'::character varying, 'OPPHØR_SØKER_OG_BARN_IKKE_LENGER_MEDLEM_TRYGDEAVTALE'::character varying, 'OPPHØR_SØKER_OG_BARN_IKKE_LENGER_FRIVILLIG_MEDLEM'::character varying, 'OPPHØR_VURDERING_ANNEN_FORELDER_IKKE_MEDLEM'::character varying, 'OPPHØR_VURDERING_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_TO_ÅR'::character varying, 'OPPHØR_VURDERING_SØKER_OG_BARN_IKKE_MEDLEM'::character varying, 'OPPHØR_SØKER_OG_BARN_IKKE_MEDLEM'::character varying, 'OPPHØR_VURDERING_FLERE_KORTE_OPPHOLD_I_UTLANDET_SISTE_ÅRENE'::character varying, 'OPPHØR_ANNEN_FORELDER_IKKE_LENGER_FRIVILLIG_MEDLEM'::character varying, 'OPPHØR_FORELDRENE_BOR_SAMMEN'::character varying, 'OPPHØR_AVTALE_OM_FAST_BOSTED'::character varying, 'OPPHØR_RETTSAVGJØRELSE_FAST_BOSTED'::character varying, 'OPPHØR_IKKE_AVTALE_OM_DELT_BOSTED'::character varying, 'OPPHØR_VURDERING_FORELDRENE_BOR_SAMMEN'::character varying, 'OPPHØR_FORELDRENE_BODD_SAMMEN'::character varying, 'OPPHØR_IKKE_OPPHOLDSTILLATELSE'::character varying, 'OPPHØR_VURDERING_FORELDRENE_BODDE_SAMMEN'::character varying, 'OPPHØR_IKKE_BOSATT_I_NORGE'::character varying, 'OPPHØR_BARN_BODDE_IKKE_MED_SØKER'::character varying, 'OPPHØR_AVTALE_DELT_BOSTED_IKKE_GYLDIG'::character varying, 'OPPHØR_VURDERING_VAR_IKKE_MEDLEM'::character varying, 'OPPHØR_VAR_IKKE_MEDLEM'::character varying, 'OPPHØR_VURDERING_DEN_ANDRE_FORELDEREN_VAR_IKKE_MEDLEM'::character varying, 'OPPHØR_AVTALE_DELT_BOSTED_FØLGES_IKKE'::character varying, 'OPPHØR_DEN_ANDRE_FORELDEREN_VAR_IKKE_MEDLEM'::character varying, 'OPPHØR_IKKE_OPPHOLDSRETT_EØS_BORGER'::character varying, 'OPPHØR_BOSATT_I_NORGE_VAR_IKKE_MEDLEM'::character varying, 'OPPHØR_BARN_DØD_SAMME_MÅNED_SOM_FØDT'::character varying, 'OPPHØR_UGYLDIG_KONTONUMMER'::character varying, 'OPPHØR_FORELDRENE_BOR_SAMMEN_ENDRE_MOTTAKER'::character varying, 'OPPHØR_SØKER_BER_OM_OPPHØR'::character varying, 'OPPHØR_IKKE_OPPHOLDSTILLATELSE_MER_ENN_12_MÅNEDER'::character varying, 'OPPHØR_DELT_BOSTED_SØKER_BER_OM_OPPHØR'::character varying, 'OPPHØR_FAST_BOSTED_AVTALE'::character varying, 'OPPHØR_BEGGE_FORELDRE_FÅTT_BARNETRYGD'::character varying, 'OPPHOR_BARNET_BOR_I_INSTITUSJON'::character varying, 'OPPHØR_BARN_BOR_IKKE_MED_SØKER_ETTER_DELT_BOSTED'::character varying, 'OPPHØR_VURDERING_IKKE_BOSATT_I_NORGE'::character varying, 'FORTSATT_INNVILGET_SØKER_OG_BARN_BOSATT_I_RIKET'::character varying, 'FORTSATT_INNVILGET_SØKER_BOSATT_I_RIKET'::character varying, 'FORTSATT_INNVILGET_BARN_BOSATT_I_RIKET'::character varying, 'FORTSATT_INNVILGET_BARN_OG_SØKER_LOVLIG_OPPHOLD_OPPHOLDSTILLATELSE'::character varying, 'FORTSATT_INNVILGET_SØKER_LOVLIG_OPPHOLD_OPPHOLDSTILLATELSE'::character varying, 'FORTSATT_INNVILGET_BARN_LOVLIG_OPPHOLD_OPPHOLDSTILLATELSE'::character varying, 'FORTSATT_INNVILGET_BOR_MED_SØKER'::character varying, 'FORTSATT_INNVILGET_FAST_OMSORG'::character varying, 'FORTSATT_INNVILGET_LOVLIG_OPPHOLD_EØS'::character varying, 'FORTSATT_INNVILGET_LOVLIG_OPPHOLD_TREDJELANDSBORGER'::character varying, 'FORTSATT_INNVILGET_UENDRET_TRYGD'::character varying, 'FORTSATT_INNVILGET_OPPHOLD_I_UTLANDET_IKKE_MER_ENN_3_MÅNEDER_SØKER_OG_BARN'::character varying, 'FORTSATT_INNVILGET_HELE_FAMILIEN_MEDLEM_ETTER_TRYGDEAVTALE'::character varying, 'FORTSATT_INNVILGET_OPPHOLD_I_UTLANDET_IKKE_MER_ENN_3_MÅNEDER_BARN'::character varying, 'FORTSATT_INNVILGET_DELT_BOSTED_PRAKTISERES_FORTSATT'::character varying, 'FORTSATT_INNVILGET_VURDERING_HELE_FAMILIEN_MEDLEM'::character varying, 'FORTSATT_INNVILGET_SØKER_OG_BARN_MEDLEM_ETTER_TRYGDEAVTALE'::character varying, 'FORTSATT_INNVILGET_ANNEN_FORELDER_IKKE_SØKT_OM_DELT_BARNETRYGD'::character varying, 'FORTSATT_INNVILGET_VURDERING_SØKER_OG_BARN_MEDLEM'::character varying, 'FORTSATT_INNVILGET_MEDLEM_I_FOLKETRYGDEN'::character varying, 'FORTSATT_INNVILGET_TVUNGENT_PSYKISK_HELSEVERN_GIFT'::character varying, 'FORTSATT_INNVILGET_FENGSEL_GIFT'::character varying, 'FORTSATT_INNVILGET_VURDERING_BOR_ALENE_MED_BARN'::character varying, 'FORTSATT_INNVILGET_SEPARERT'::character varying, 'FORTSATT_INNVILGET_FORTSATT_RETTSAVGJØRELSE_OM_DELT_BOSTED'::character varying, 'FORTSATT_INNVILGET_BOR_ALENE_MED_BARN'::character varying, 'FORTSATT_INNVILGET_TVUNGENT_PSYKISK_HELSEVERN_SAMBOER'::character varying, 'FORTSATT_INNVILGET_FORVARING_SAMBOER'::character varying, 'FORTSATT_INNVILGET_FORTSATT_AVTALE_OM_DELT_BOSTED'::character varying, 'FORTSATT_INNVILGET_VARETEKTSFENGSEL_SAMBOER'::character varying, 'FORTSATT_INNVILGET_FENGSEL_SAMBOER'::character varying, 'FORTSATT_INNVILGET_FORVARING_GIFT'::character varying, 'FORTSATT_INNVILGET_VAREKTEKTSFENGSEL_GIFT'::character varying, 'FORTSATT_INNVILGET_FORSVUNNET_SAMBOER'::character varying, 'FORTSATT_INNVILGET_FORSVUNNET_EKTEFELLE'::character varying, 'FORTSATT_INNVILGET_BRUKER_ER_BLITT_NORSK_STATSBORGER'::character varying, 'FORTSATT_INNVILGET_BRUKER_OG_BARN_ER_BLITT_NORSKE_STATSBORGERE'::character varying, 'FORTSATT_INNVILGET_ET_BARN_ER_BLITT_NORSK_STATSBORGER'::character varying, 'FORTSATT_INNVILGET_FLERE_BARN_ER_BLITT_NORSKE_STATSBORGERE'::character varying, 'FORTSATT_INNVILGET_OPPDATERT_KONTO_OPPLYSNINGER'::character varying, 'FORTSATT_INNVILGET_ADRESSE_REGISTRERT'::character varying, 'FORTSATT_INNVILGET_VARIG_OPPHOLDSTILLATELSE'::character varying, 'FORTSATT_INNVILGET_VARIG_OPPHOLDSRETT_EØS_BORGER'::character varying, 'FORTSATT_INNVILGET_GENERELL_BOR_SAMMEN_MED_BARN'::character varying, 'ENDRET_UTBETALINGSPERIODE_DELT_BOSTED_INGEN_UTBETALING_NY'::character varying, 'ENDRET_UTBETALINGSPERIODE_DELT_BOSTED_FULL_UTBETALING_FØR_SOKNAD_NY'::character varying, 'ENDRET_UTBETALINGSPERIODE_DELT_BOSTED_KUN_ETTERBETALT_UTVIDET_NY'::character varying, 'ENDRET_UTBETALINGSPERIODE_DELT_BOSTED_MOTTATT_FULL_ORDINÆR_ETTERBETALT_UTVIDET_NY'::character varying, 'ENDRET_UTBETALINGSPERIODE_DELT_BOSTED_ENDRET_UTBETALING'::character varying, 'ENDRET_UTBETALING_SEKUNDÆR_DELT_BOSTED_FULL_UTBETALING_FØR_SØKNAD'::character varying, 'ENDRET_UTBETALING_ETTERBETALT_UTVIDET_DEL_FRA_AVTALETIDSPUNKT_SØKT_FOR_PRAKTISERT_DELT'::character varying, 'ENDRET_UTBETALING_ALLEREDE_UTBETALT_FORELDRE_BOR_SAMMEN'::character varying, 'ENDRET_UTBETALING_ETTERBETALING_UTVIDET_EØS'::character varying, 'ENDRET_UTBETALING_OPPHØR_ENDRE_MOTTAKER'::character varying, 'ENDRET_UTBETALING_REDUKSJON_ENDRE_MOTTAKER'::character varying, 'ENDRET_UTBETALING_ETTERBETALING_TRE_ÅR_TILBAKE_I_TID'::character varying, 'ENDRET_UTBETALING_ETTERBETALING_TRE_ÅR_TILBAKE_I_TID_SED'::character varying, 'ENDRET_UTBETALING_ETTERBETALING_TRE_ÅR_TILBAKE_I_TID_KUN_UTVIDET_DEL_UTBETALING'::character varying, 'ENDRET_UTBETALING_ETTERBETALING_TRE_ÅR_TILBAKE_I_TID_SED_UTBETALING'::character varying, 'ENDRET_UTBETALING_TRE_ÅR_TILBAKE_I_TID_UTBETALING'::character varying, 'ETTER_ENDRET_UTBETALING_RETTSAVGJØRELSE_DELT_BOSTED'::character varying, 'ETTER_ENDRET_UTBETALING_AVTALE_DELT_BOSTED_FØLGES'::character varying, 'ETTER_ENDRET_UTBETALING_HAR_AVTALE_DELT_BOSTED'::character varying, 'ETTER_ENDRET_UTBETALING_ETTERBETALING'::character varying, 'ETTER_ENDRET_UTBETALING_ETTERBETALING_UTVIDET'::character varying, 'ETTER_ENDRET_UTBETALING_ETTERBETALING_SED'::character varying, 'ETTER_ENDRET_UTBETALING_ETTERBETALING_TRE_AAR'::character varying, 'ETTER_ENDRET_UTBETALING_EØS_BARNETRYGD_ALLEREDE_UTBETALT'::character varying, 'ETTER_ENDRET_UTBETALING_ETTERBETALING_TRE_AAR_KUN_UTVIDET_DEL'::character varying, 'INNVILGET_BOR_FAST_I_INSTITUSJON'::character varying, 'INNVILGET_SATSENDRING_INSTITUSJON'::character varying, 'REDUKSJON_BARN_6_ÅR_INSTITUSJON'::character varying, 'REDUKSJON_SATSENDRING_INSTITUSJON'::character varying, 'AVSLAG_IKKE_BOSATT_I_INSTITUSJON'::character varying, 'AVSLAG_IKKE_OPPHOLDSTILLATELSE_INSTITUSJON'::character varying, 'OPPHØR_FLYTTET_FRA_INSTITUSJON'::character varying, 'OPPHØR_BARN_DØD_INSTITUSJON'::character varying, 'OPPHØR_BARN_BODDE_IKKE_FAST_I_INSTITUSJON'::character varying, 'OPPHØR_BARN_HADDE_IKKE_OPPHOLDSTILLATELSE_INSTITUSJON'::character varying, 'OPPHØR_OPPHOLDSTILLATELSE_UTLØPT_INSTITUSJON'::character varying, 'OPPHØR_BARNET_ER_18_ÅR_INSTITUSJON'::character varying, 'FORTSATT_INNVILGET_BOSATT_I_INSTITUSJON'::character varying, 'FORTSATT_INNVILGET_OPPHOLDSTILLATELSE_INSTITUSJON'::character varying, 'FORTSATT_INNVILGET_VARIG_OPPHOLDSTILLATELSE_INSTITUSJON'::character varying, 'FORTSATT_INNVILGET_NORSK_STATSBORGER_INSTITUSJON'::character varying])::text[]))";
        //String sql = "((status)::text = ANY ((ARRAY['OPPRETTET'::character varying, 'LØPENDE'::character varying, 'AVSLUTTET'::character varying])::text[]))";

//        String sql = "((foo)::text = ANY ((ARRAY['bar'])::text[]))";
//        String sql = "((foo)::text = ANY ((ARRAY['bar'])))";
        String sql = "((foo)::text = ANY (ARRAY['bar']))";

        Expression expression = CCJSqlParserUtil.parseCondExpression(sql, false);
//        SqlCondition actual = parse(sql, ConstraintDatabaseType.POSTGRES);
    }
}
