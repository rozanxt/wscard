package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.input.InputManager;
import zan.lib.net.NetworkManager;
import zan.lib.core.BasePanel;
import static zan.lib.input.InputManager.*;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.gui.GameGUI;
import zan.wscard.sys.NetworkGameClient;
import zan.wscard.sys.NetworkGameServer;
import zan.wscard.sys.PlayerInfo;

public class GamePanel extends BasePanel {

	private GameCore gameCore;

	private DefaultShader shaderProgram;
	private ViewPort2D viewPort;

	private NetworkGameServer gameServer;
	private NetworkGameClient gameClient;

	private GameGUI gameGUI;

	// TODO
	private int mode;
	private String address;

	public GamePanel(GameCore core, int mode, String address) {
		gameCore = core;
		shaderProgram = new DefaultShader();
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
		this.mode = mode;
		this.address = address;
	}

	@Override
	public void init() {
		// Initialization
		shaderProgram.loadProgram();
		shaderProgram.enableBlend(true);

		viewPort.setHeightInterval(600.0);
		viewPort.showView();
		viewPort.projectView(shaderProgram);

		// Card data / image
		CardReader cr = new CardReader();
		ArrayList<CardData> LHCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> LHDeck = new ArrayList<CardData>();
		final int[] LHSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<LHSetup.length;i++) {
			for (int j=0;j<LHSetup[i];j++) {
				LHDeck.add(LHCards.get(i));
			}
		}
		ArrayList<CardData> PDCards = cr.loadCardData("res/card/PD.wsci");
		ArrayList<CardData> PDDeck = new ArrayList<CardData>();
		final int[] PDSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<PDSetup.length;i++) {
			for (int j=0;j<PDSetup[i];j++) {
				PDDeck.add(PDCards.get(i));
			}
		}

		TextureManager.loadTexture("CARDBACK", "res/img/card/cardback.jpg");
		TextureManager.loadTexture("CARDFIELD", "res/img/card/cardfield.png");
		TextureManager.loadTexture("CARDCLOCK", "res/img/card/cardclock.png");
		for (int i=0;i<LHCards.size();i++) {
			CardData c = LHCards.get(i);
			TextureManager.loadTexture(c.id, c.image);
		}
		for (int i=0;i<PDCards.size();i++) {
			CardData c = PDCards.get(i);
			TextureManager.loadTexture(c.id, c.image);
		}

		// Server & Client
		PlayerInfo infoA = new PlayerInfo("Player A", LHDeck);
		PlayerInfo infoB = new PlayerInfo("Player B", PDDeck);

		int port = 3276;
		if (mode == 1) {
			if (NetworkManager.openServer(port, 2)) {
				if (NetworkManager.openClient(address, port)) {
					gameServer = new NetworkGameServer();
					gameServer.initServer(infoA, infoB);
					gameClient = new NetworkGameClient();
					gameClient.initClient(infoA, infoB);
				} else {
					NetworkManager.closeServer();
				}
			}
		} else if (mode == 2) {
			if (NetworkManager.openClient(address, port)) {
				gameServer = null;
				gameClient = new NetworkGameClient();
				gameClient.initClient(infoB, infoA);
			}
		}

		gameGUI = new GameGUI(gameClient);
	}

	@Override
	public void destroy() {
		gameGUI.destroy();
		shaderProgram.destroy();
		NetworkManager.closeClient();
		if (mode == 1) NetworkManager.closeServer();
	}

	@Override
	public void update(double time) {
		if (InputManager.isKeyReleased(InputManager.IM_KEY_ESCAPE)) gameCore.close();
		else if (InputManager.isKeyReleased(InputManager.IM_KEY_F11)) gameCore.toggleFullScreen();

		gameGUI.updateMousePos(viewPort.getScreenToVirtualX(getMouseX()), viewPort.getScreenToVirtualY(gameCore.getScreenHeight()-getMouseY()));
		gameGUI.doUserInterface();
		gameGUI.doActionEvents();
		gameGUI.update();

		if (mode == 1) gameServer.update();
		gameClient.update();
	}

	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);

		gameGUI.render(shaderProgram, ip);

		shaderProgram.popMatrix();
		shaderProgram.unbind();
	}

	@Override
	public void onScreenResize(int width, int height) {
		shaderProgram.bindState();
		viewPort.setScreenSize(width, height);
		viewPort.setViewPort(0, 0, width, height);
		viewPort.showView();
		viewPort.projectView(shaderProgram);
	}

}
