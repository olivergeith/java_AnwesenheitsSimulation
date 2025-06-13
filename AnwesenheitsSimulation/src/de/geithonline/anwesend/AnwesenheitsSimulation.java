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
import java.awt.Color;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class AnwesenheitsSimulation extends JFrame implements NativeKeyListener {

	private static final int START_DELAY = 30 * 1000; // in Millisekunden
	private static final long serialVersionUID = 1815021257113792239L;
	private Robot robot; // NOSONAR
	private int delta = 0;
	private int vorzeichen = 1;
	private boolean isRunning = false;
	private final JLabel label = new JLabel("---");
	private final JLabel labelImg = new JLabel("");
	private Point lastLocation = new Point(0, 0);
	private long notMovedSince = 0;

	URL gifUrl = getClass().getResource("working4.gif");
	private final ImageIcon working = new ImageIcon(gifUrl);

	public void addComponentsToPane(final Container container) {
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(label);
		container.add(labelImg);
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
		setRunning(false);
		// register global keylistener
		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
		} catch (final NativeHookException e1) {
			e1.printStackTrace();
		}

		// show window
		setVisible(true);

		// endlosloop zur Maousüberwachung.
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
				label.setText("User inactive for: " + timeNotMoved / 1000 + " seconds");
				if (timeNotMoved > START_DELAY) {
					// wenn länger die Mous nicht bewegt wurde
					setRunning(true);
				}
			}
		} else {
			setUserWasActive("");
			if (isRunning && isMousOutsideFrame(currentLocation)) {
				setRunning(false);
			}
		}
		lastLocation = currentLocation;
	}

	private void setUserWasActive(final String what) {
		notMovedSince = 0;
	}

	private void setRunning(final boolean isRun) {
		isRunning = isRun;
		label.setText("Simulating activity: " + isRunning);
		if (isRun) {
			labelImg.setIcon(working);
			getContentPane().setBackground(new Color(128, 255, 128));
		} else {
			getContentPane().setBackground(Color.LIGHT_GRAY);
			labelImg.setIcon(null);
		}
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

	@Override
	public void nativeKeyPressed(final NativeKeyEvent e) {
		setUserWasActive("Keay pressd " + NativeKeyEvent.getKeyText(e.getKeyCode()));
	}

	public static void main(final String[] args) {
		new AnwesenheitsSimulation();
	}
}