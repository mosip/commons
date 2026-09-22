package io.mosip.kernel.core.packetuploader.spi;

/**
 * Uploads MOSIP registration packets over SFTP.
 * <p>
 * Contract: implementations open an SFTP channel, upload a local file, and
 * release the connection. {@code source} must be a non-blank existing path.
 * The remote destination folder must already exist. Call from registration
 * client when sending packets to the landing zone.
 * </p>
 *
 * @param <S> SFTP server configuration type
 * @param <C> SFTP channel / session type
 * @author Urvil Joshi
 * @since 1.0.0
 */
public interface PacketUploader<S, C> {

	/**
	 * Connects an SFTP channel using the given server configuration.
	 *
	 * @param sftpServer never-null host/user/key configuration
	 * @return never-null connected channel
	 * @throws io.mosip.kernel.core.packetuploader.exception.ConnectionException when the server cannot be reached
	 */
	C createSFTPChannel(S sftpServer);

	/**
	 * Uploads the local packet at {@code source} through {@code sftpChannel}.
	 *
	 * @param sftpChannel never-null connected channel
	 * @param source      never-null, never-blank local packet path
	 * @throws io.mosip.kernel.core.packetuploader.exception.NullPathException when {@code source} is null
	 * @throws io.mosip.kernel.core.packetuploader.exception.EmptyPathException when {@code source} is empty
	 * @throws io.mosip.kernel.core.packetuploader.exception.SFTPException when the transfer fails
	 */
	void upload(C sftpChannel, String source);

	/**
	 * Closes the SFTP channel.
	 *
	 * @param sftpChannel never-null channel previously returned by {@link #createSFTPChannel(Object)}
	 */
	void releaseConnection(C sftpChannel);

}
