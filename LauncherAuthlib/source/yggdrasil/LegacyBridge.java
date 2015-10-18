package com.mojang.authlib.yggdrasil;

import launcher.client.ClientLauncher;
import launcher.helper.CommonHelper;
import launcher.helper.LogHelper;
import launcher.request.auth.CheckServerRequest;
import launcher.request.auth.JoinServerRequest;

public final class LegacyBridge {
	private LegacyBridge() {
	}

	@SuppressWarnings("unused")
	public static boolean checkServer(String username, String serverID) throws Exception {
		LogHelper.debug("LegacyBridge.checkServer, Username: '%s', server ID: %s", username, serverID);
		return new CheckServerRequest(username, serverID).request() != null;
	}

	@SuppressWarnings("unused")
	public static String getCloakURL(String username) {
		LogHelper.debug("LegacyBridge.getCloakURL: '%s'", username);
		return CommonHelper.replace(System.getProperty("launcher.legacy.cloaksURL",
			"http://skins.minecraft.net/MinecraftCloaks/%username%.png"), "username", username);
	}

	@SuppressWarnings("unused")
	public static String getSkinURL(String username) {
		LogHelper.debug("LegacyBridge.getSkinURL: '%s'", username);
		return CommonHelper.replace(System.getProperty("launcher.legacy.skinsURL",
			"http://skins.minecraft.net/MinecraftSkins/%username%.png"), "username", username);
	}

	@SuppressWarnings("unused")
	public static String joinServer(String username, String accessToken, String serverID) {
		if (!ClientLauncher.isLaunched()) {
			return "Bad Login (Cheater)";
		}

		// Join server
		LogHelper.debug("LegacyBridge.joinServer, Username: '%s', access token: %s, server ID: %s", username, accessToken, serverID);
		try {
			return new JoinServerRequest(username, accessToken, serverID).request() ? "OK" : "Bad Login (Clientside)";
		} catch (Exception e) {
			return e.toString();
		}
	}
}