import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Main {

	public static void main(String[] args) {
		String tmp = "point at 4,4\nline from 0,2 to 1,1";
		Main main = new Main();
		main.parse(tmp);
	}

	Main() { /* nothing */ }

	private void parse(String c) {
		// create token stream from lexer
		CommonTokenStream ts = new CommonTokenStream(new TestLLexer(CharStreams.fromString(c)));

		// create the parser
		TestLParser parser = new TestLParser(ts);

		// parse, by requesting the root node
		ParseTree tree =  parser.file();
		System.out.println(tree.toStringTree(parser));

		// use a visitor
		CountPointsVisitor cv = new CountPointsVisitor();
		Integer n = cv.visit(tree);
		System.out.println(n);

		n = tree.accept(cv); // same!
		System.out.println(n);

		// use a listener
		ParseTreeWalker walker = new ParseTreeWalker();
		CountPointsListener li = new CountPointsListener();
		walker.walk(li, tree);
		System.out.println(li.n);
	}
}
