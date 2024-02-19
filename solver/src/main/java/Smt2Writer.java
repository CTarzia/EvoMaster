import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType;
import org.evomaster.client.java.controller.api.dto.database.schema.TableCheckExpressionDto;
import org.evomaster.dbconstraint.ConstraintDatabaseType;
import org.evomaster.dbconstraint.ast.SqlComparisonCondition;
import org.evomaster.dbconstraint.ast.SqlCondition;
import org.evomaster.dbconstraint.parser.jsql.JSqlConditionParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A writer for SMT2 format.
 * It is used to write the constraints in a file that can be read by any smt2 solver.
 * The parsing and the writing of the constraints is done hardcoded here and not using a library.
 * This is because for the purpose of the feature, it is not necessary to use an external library.
 */
public class Smt2Writer  {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Smt2Writer.class.getName());

    // The variables that solve the constraint
    private final List<String> variables = new ArrayList<>();

    // The assertions that those values need to satisfy
    private final List<String> constraints = new ArrayList<>();
    private final JSqlConditionParser parser;
    private final ConstraintDatabaseType dbType;

    Smt2Writer(DatabaseType databaseType) {
        this.parser = new JSqlConditionParser();
        this.dbType = ConstraintDatabaseType.valueOf(databaseType.name());
    }

    /**
     * Writes the content of the Smt2Writer in a file with the given filename
     * @param filename the name of the file
     */
    public void writeToFile(String filename) {
        String text = asText();
        try {
            Files.write(Paths.get(filename), text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String asText() {
        StringBuilder sb = new StringBuilder();

        sb.append("(set-logic QF_SLIA)\n");

        declareConstants(sb);
        assertConstraints(sb);

        sb.append("(check-sat)\n");
        getValues(sb);

        return sb.toString();
    }

    private void declareConstants(StringBuilder sb) {
        for (String value: this.variables) {
            sb.append("(declare-const ");
            sb.append(value);
            sb.append(" Int)\n");
        }
    }

    private void assertConstraints(StringBuilder sb) {
        for (String constraint : this.constraints) {
            sb.append("(assert ");
            sb.append(constraint);
            sb.append(")\n");
        }
    }

    private void getValues(StringBuilder sb) {
        for (String value : this.variables) {
            sb.append("(get-value (");
            sb.append(value);
            sb.append("))\n");
        }
    }

    public boolean addTableCheckExpression(TableCheckExpressionDto checkConstraint) {
        try {

            SqlCondition condition = parser.parse(checkConstraint.sqlCheckExpression, this.dbType);

            if (!(condition instanceof SqlComparisonCondition)) {
                // TODO: Support other check expressions
                throw new RuntimeException("The condition is not a comparison condition");
            }
            SqlComparisonCondition comparisonCondition = (SqlComparisonCondition) condition;

            String variable = comparisonCondition.getLeftOperand().toString();
            String compare = comparisonCondition.getRightOperand().toString();
            String comparator = comparisonCondition.getSqlComparisonOperator().toString();

            this.variables.add(variable);
            this.constraints.add("(" + comparator + " " + variable + " " + compare + ")");

            return true;

        } catch (Exception e) {
            log.error(String.format("There was an error parsing the constraint %s", e.getMessage()));
            return false;
        }
    }
}
