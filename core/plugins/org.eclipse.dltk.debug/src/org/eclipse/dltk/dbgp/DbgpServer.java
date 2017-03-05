package org.eclipse.dltk.dbgp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.dbgp.internal.DbgpDebugingEngine;
import org.eclipse.dltk.dbgp.internal.DbgpSession;
import org.eclipse.dltk.dbgp.internal.DbgpWorkingThread;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;

public class DbgpServer extends DbgpWorkingThread {
	private final int port;
	private ServerSocket server;

	private final int clientTimeout;

	public static int findAvailablePort(int fromPort, int toPort) {
		if (fromPort > toPort) {
			throw new IllegalArgumentException(
					Messages.DbgpServer_startPortShouldBeLessThanOrEqualToEndPort);
		}

		int port = fromPort;
		ServerSocket socket = null;
		while (port <= toPort) {
			try {
				socket = new ServerSocket(port);
				return port;
			} catch (IOException e) {
				++port;
			} finally {
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		return -1;
	}

	private static final int STATE_NONE = 0;
	private static final int STATE_STARTED = 1;
	private static final int STATE_CLOSED = 2;

	private final Object stateLock = new Object();
	private int state = STATE_NONE;

	public boolean isStarted() {
		synchronized (stateLock) {
			return state == STATE_STARTED;
		}
	}

	public boolean waitStarted() {
		return waitStarted(15000);
	}

	public boolean waitStarted(long timeout) {
		synchronized (stateLock) {
			if (state == STATE_STARTED) {
				return true;
			} else if (state == STATE_CLOSED) {
				return false;
			}
			try {
				stateLock.wait(timeout);
			} catch (InterruptedException e) {
				// ignore
			}
			return state == STATE_STARTED;
		}
	}

	@Override
	protected void workingCycle() throws Exception, IOException {
		try {
			server = new ServerSocket(port);
			synchronized (stateLock) {
				state = STATE_STARTED;
				stateLock.notifyAll();
			}
			while (!server.isClosed()) {
				final Socket client = server.accept();
				client.setSoTimeout(clientTimeout);
				createSession(client);
			}
		} finally {
			if (server != null && !server.isClosed()) {
				server.close();
			}
			synchronized (stateLock) {
				state = STATE_CLOSED;
				stateLock.notifyAll();
			}
		}
	}

	private static final class DbgpSessionJob extends Job {
		private final Socket client;
		private final IDbgpServerListener listener;

		private DbgpSessionJob(Socket client, IDbgpServerListener listener) {
			super(Messages.DbgpServer_acceptingDebuggingEngineConnection);
			this.client = client;
			this.listener = listener;
			setSystem(true);
		}

		@Override
		public boolean shouldSchedule() {
			return listener != null;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DbgpDebugingEngine engine = null;
			try {
				engine = new DbgpDebugingEngine(client);
				DbgpSession session = new DbgpSession(engine);
				listener.clientConnected(session);
			} catch (Exception e) {
				DLTKDebugPlugin.log(e);
				if (engine != null)
					engine.requestTermination();
			}
			return Status.OK_STATUS;
		}
	}

	private void createSession(final Socket client) {
		Job job = new DbgpSessionJob(client, listener);
		job.schedule();
	}

	public DbgpServer(int port, int clientTimeout) {
		super("DbgpServer"); //$NON-NLS-1$

		this.port = port;
		this.clientTimeout = clientTimeout;
	}

	/**
	 * @param port
	 * @param serverTimeout
	 * @param clientTimeout
	 * @deprecated use {@link #DbgpServer(int, int)}
	 */
	@Deprecated
	public DbgpServer(int port, int serverTimeout, int clientTimeout) {
		this(port, clientTimeout);
	}

	@Override
	public void requestTermination() {
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			DLTKDebugPlugin.log(e);
		}
		super.requestTermination();
	}

	private IDbgpServerListener listener;

	public void setListener(IDbgpServerListener listener) {
		this.listener = listener;
	}
}
