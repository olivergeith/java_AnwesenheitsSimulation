// ******************************************************************************
//
// Copyright (c) 2025 by
// Scheidt & Bachmann System Technik GmbH, 24109 Melsdorf
//
// All rights reserved. The reproduction, distribution and utilisation of this document
// as well as the communication of its contents to others without explicit authorisation
// is prohibited. Offenders will be held liable for the payment of damages.
// All rights reserved in the event of the grant of a patent, utility model or design.
// (DIN ISO 16016:2007-12, Section 5.1)
//
// Alle Rechte vorbehalten. Weitergabe sowie Vervielfältigung dieses Dokuments,
// Verwertung und Mitteilung seines Inhalts sind verboten, soweit nicht ausdrücklich
// gestattet. Zuwiderhandlungen verpflichten zu Schadenersatz. Alle Rechte für den
// Fall der Patent-, Geschmacks- und Gebrauchsmustererteilung vorbehalten.
// (DIN ISO 16016:2007-12, Abschnitt 5.1)
//
// ******************************************************************************

package de.geithonline.anwesend;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class AnwesenheitsSimulation extends JFrame {

	private static final int START_DELAY = 30 * 1000; // in Millisekunden
	private static final long serialVersionUID = 1815021257113792239L;
	private Robot robot; // NOSONAR
	private int delta = 0;
	private int vorzeichen = 1;
	private boolean isRunning = false;
	private final JLabel label = new JLabel("Running " + isRunning);
	private final JLabel labelStatus = new JLabel("---");
	private Point lastLocation = new Point(0, 0);
	private long notMovedSince = 0;

	public void addComponentsToPane(final Container container) {
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(label);
		container.add(labelStatus);
	}

	/**
	 * Constructor.
	 */
	public AnwesenheitsSimulation() {
		super("Anwesenheits Simulation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 300); // Breite: 400 Pixel, Höhe: 300 Pixel
		setLocation(10, 10);
		// setLocationRelativeTo(null); // mitte des Schirms
		setResizable(false);

		addComponentsToPane(getContentPane());
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				setRunning(!isRunning);
			}
		});
		setVisible(true);
		try {
			robot = new Robot();
			while (true) { // NOSONAR
				moveMouse();
				Thread.sleep(500);
			}
		} catch (final AWTException | InterruptedException e) { // NOSONAR
			e.printStackTrace();
		}
	}

	private void moveMouse() {
		check4Inactivity();
		if (!isRunning) {
			return;
		}
		final Point location = getLocation();
		final int centerX = location.x + getSize().width / 2 + delta;
		final int centerY = location.y + getSize().height / 2;
		robot.mouseMove(centerX, centerY);
		increaseDelta();
	}

	private boolean isMousOutsideFrame(final Point currentLocation) {
		final Point location = getLocation();
		return currentLocation.x < location.x //
				|| currentLocation.y < location.y//
				|| currentLocation.x > location.x + getSize().width //
				|| currentLocation.y > location.y + getSize().height;
	}

	private void check4Inactivity() {
		final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		// Location-Objekt (Point) vom PointerInfo erhalten
		final Point currentLocation = pointerInfo.getLocation();
		if (lastLocation.x == currentLocation.x && lastLocation.y == currentLocation.y) {
			// mouse wurde nicht bewegt!
			final long timeMillis = System.currentTimeMillis();
			if (notMovedSince == 0) {
				notMovedSince = timeMillis;
			} else {
				final long timeNotMoved = timeMillis - notMovedSince;
				labelStatus.setText("Mouse not moving: " + timeNotMoved / 1000 + "s");
				if (timeNotMoved > START_DELAY) {
					// wenn länger die Mous nicht bewegt wurde
					setRunning(true);
				}
			}
		} else {
			notMovedSince = 0;
			labelStatus.setText("Mouse is moved");
			if (isRunning && isMousOutsideFrame(currentLocation)) {
				setRunning(false);
			}
		}
		lastLocation = currentLocation;
	}

	private void setRunning(final boolean isRun) {
		isRunning = isRun;
		label.setText("Running " + isRunning);
	}

	private void increaseDelta() {
		delta = delta + vorzeichen * 5;
		if (delta >= 100) {
			vorzeichen = -1;
		}
		if (delta <= -100) {
			vorzeichen = 1;
		}
	}

	public static void main(final String[] args) {
		new AnwesenheitsSimulation();
	}
}