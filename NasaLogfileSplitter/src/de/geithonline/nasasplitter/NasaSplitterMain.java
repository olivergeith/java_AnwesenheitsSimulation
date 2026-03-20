package de.geithonline.nasasplitter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class NasaSplitterMain extends JFrame {

	public NasaSplitterMain() {
		super("NasaLogSplitter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 300); // Breite: 400 Pixel, Höhe: 300 Pixel
		setLocation(10, 10);
		// setLocationRelativeTo(null); // mitte des Schirms
		setResizable(false);

		// 2. Erstellen des Dateiauswahl-Dialogs
		final JFileChooser fileChooser = new JFileChooser(
				"C:\\dataP\\Arbeit\\Haus&Hof\\Wärmepumpe\\NASAmonitor\\LogFiles");

		// 3. Titel des Dialogs festlegen
		fileChooser.setDialogTitle("Bitte wählen Sie eine Log-Datei aus");

		// 4. Filter hinzufügen, damit nur .log Dateien (und .txt) angezeigt werden
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("Log Dateien (*.log)", "log");
		fileChooser.setFileFilter(filter);

		// 5. Dialog öffnen (null bedeutet: zentriert auf dem Bildschirm)
		final int result = fileChooser.showOpenDialog(null);

		// 6. Prüfen, ob der Benutzer "Öffnen" geklickt hat
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();

			System.out.println("-----------------------------------------");
			System.out.println("Datei erfolgreich ausgewählt!");
			System.out.println("Pfad: " + selectedFile.getAbsolutePath());
			System.out.println("-----------------------------------------");

			split(selectedFile);
		} else {
			System.out.println("Der Vorgang wurde vom Benutzer abgebrochen.");
		}

	}

	private final Map<String, List<String>> linemap = new HashMap<>();

	private void split(final File selectedFile) {
		linemap.clear();
		try (Stream<String> lines = Files.lines(selectedFile.toPath())) {
			lines.forEach(line -> {
				if (line.length() > 12) {
					final String date = line.substring(1, 11);
					System.out.println("date " + date);
					add2LineMap(date, line);
				}
			});
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final File folder = selectedFile.getParentFile();

		linemap.entrySet().forEach(entry -> {
			final String date = entry.getKey();
			final List<String> lines4Date = entry.getValue();
			final File logFile4Date = new File(folder, date + " NASA Monitor LogFile(splitted).log");
			try {
				System.out.println("Writing: " + logFile4Date);
				Files.write(logFile4Date.toPath(), lines4Date, StandardCharsets.UTF_8);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void add2LineMap(final String date, final String line) {
		List<String> lines = linemap.get(date);
		if (lines == null) {
			lines = new ArrayList<String>();
			linemap.put(date, lines);
		}
		lines.add(line);
	}

	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
		new NasaSplitterMain();

	}
}
