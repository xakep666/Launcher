package launchserver.auth.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.helper.LineReader;

public final class FileAuthProvider extends DigestAuthProvider {
	private final Path file;

	// Cache
	private final Map<String, String> cache = new HashMap<>(8192);
	private final Object cacheLock = new Object();
	private FileTime cacheLastModified;

	public FileAuthProvider(BlockConfigEntry block) {
		super(block);
		file = IOHelper.toPath(block.getEntryValue("file", StringConfigEntry.class));
	}

	@Override
	public String auth(String login, String password) throws IOException {
		String validDigest;
		synchronized (cacheLock) {
			updateCache();
			validDigest = cache.get(login);
		}
		verifyDigest(validDigest, password);
		return login;
	}

	@Override
	public void flush() {
		// Do nothing
	}

	private void updateCache() throws IOException {
		FileTime lastModified = IOHelper.readAttributes(file).lastModifiedTime();
		if (lastModified.equals(cacheLastModified)) {
			return; // Not modified, so cache is up-to-date
		}

		// Read file
		cache.clear();
		LogHelper.info("Recaching users file: '%s'", file);
		try (BufferedReader reader = new LineReader(IOHelper.newReader(file))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				// Get and verify split index
				int splitIndex = line.indexOf(':');
				if (splitIndex < 0) {
					throw new IOException(String.format("Illegal line in users file: '%s'", line));
				}

				// Split and verify username and password
				String username = line.substring(0, splitIndex).trim();
				String password = line.substring(splitIndex + 1).trim();
				if (username.isEmpty() || password.isEmpty()) {
					throw new IOException(String.format("Empty username or password in users file: '%s'", line));
				}

				// Try put to cache
				if (cache.put(username, password) != null) {
					throw new IOException(String.format("Duplicate username in users file: '%s'", username));
				}
			}
		}

		// Update last modified time
		cacheLastModified = lastModified;
	}
}