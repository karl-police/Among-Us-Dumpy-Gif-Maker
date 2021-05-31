import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class Controller implements Initializable {
	
	public TextArea ProgressConsoleTextArea;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream printStream = new PrintStream(baos);
	    
	    PrintStream printStreamOut = new ConsoleOutput(printStream, ProgressConsoleTextArea);
	    System.setOut(printStreamOut);
	    System.setErr(printStreamOut);
	}
	
	public Button ProcessOkay_button;
	public Button CancelProcess_button;
	
	public void handleProcessOkay() {
		System.exit(0);
	}
	
	public void handleCancelProcess() {
		System.exit(0);
	}
	
	
	class ConsoleOutput extends PrintStream {
		private TextArea textArea;
		
		public ConsoleOutput(PrintStream ps, TextArea textArea) {
			super(ps);
			this.textArea = textArea;
		}
		
		@Override
		public void write(byte[] buf, int off, int len) {
			final String message = new String(buf, off, len);
			Platform.runLater(()->textArea.appendText(message));
        }
	}
}