/* Count the number of points in a file, but with a listener
 * Some would say it is easier.
 */
public class CountPointsListener extends TestLBaseListener {
	public int n = 0;

	@Override
	public void enterCline(TestLParser.ClineContext ctx) { n += 2; }

	@Override
	public void enterCpoint(TestLParser.CpointContext ctx) { n += 1; }
}
