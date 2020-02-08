/* Count the total number of point in a file */
public class CountPointsVisitor extends TestLBaseVisitor<Integer> {
	public Integer visitFile(TestLParser.FileContext ctx) { return visit(ctx.commands()); /* note: not visiting EOF! */ }
	public Integer visitCommands(TestLParser.CommandsContext ctx) {
		/* visit the command ...*/
		Integer n = visit(ctx.command());
		/* ... And the next one, but ONLY if it exists */
		if (ctx.commands() != null)
			n += visit(ctx.commands());
		return n;
	}
	public Integer visitCline(TestLParser.ClineContext ctx) { return 2; } // cline contains 2 points
	public Integer visitCpoint(TestLParser.CpointContext ctx) { return 1; } // cpoint contain only 1 point

	/* notice that `visitPoint()` is never called! */
}