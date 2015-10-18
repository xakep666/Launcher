package launchserver.auth.handler;

import java.io.Flushable;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;

public abstract class AuthHandler extends ConfigObject implements Flushable {
	private static final Map<String, Adapter<AuthHandler>> AUTH_HANDLERS = new ConcurrentHashMap<>(4);

	@LauncherAPI
	protected AuthHandler(BlockConfigEntry block) {
		super(block);
	}

	@LauncherAPI
	public abstract UUID auth(String username, String accessToken) throws IOException;

	@LauncherAPI
	public abstract UUID checkServer(String username, String serverID) throws IOException;

	@LauncherAPI
	public abstract boolean joinServer(String username, String accessToken, String serverID) throws IOException;

	@LauncherAPI
	public abstract UUID usernameToUUID(String username) throws IOException;

	@LauncherAPI
	public abstract String uuidToUsername(UUID uuid) throws IOException;

	@LauncherAPI
	public static AuthHandler newHandler(String name, BlockConfigEntry block) {
		Adapter<AuthHandler> authHandlerAdapter = VerifyHelper.getMapValue(AUTH_HANDLERS, name,
			String.format("Unknown auth handler: '%s'", name));
		return authHandlerAdapter.convert(block);
	}

	@LauncherAPI
	public static void registerHandler(String name, Adapter<AuthHandler> adapter) {
		VerifyHelper.verifyIDName(name);
		VerifyHelper.verify(AUTH_HANDLERS.putIfAbsent(name, Objects.requireNonNull(adapter, "adapter")),
			a -> a == null, String.format("Auth handler has been already registered: '%s'", name));
	}

	static {
		registerHandler("null", NullAuthHandler::new);
		registerHandler("binaryFile", BinaryFileAuthHandler::new);
		registerHandler("textFile", TextFileAuthHandler::new);
		registerHandler("mysql", MySQLAuthHandler::new);
	}
}