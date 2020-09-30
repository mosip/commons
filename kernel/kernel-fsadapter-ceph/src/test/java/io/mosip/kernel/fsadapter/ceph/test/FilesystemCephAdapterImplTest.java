package io.mosip.kernel.fsadapter.ceph.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import io.findify.s3mock.S3Mock;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.fsadapter.ceph.constant.PacketFiles;
import io.mosip.kernel.fsadapter.ceph.impl.CephAdapterImpl;
import io.mosip.kernel.fsadapter.ceph.util.ConnectionUtils;

/**
 * This class tests the functionalities of DFSAdapterImpl.
 *
 * @author Pranav Kumar
 * @author Ranjitha
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ConnectionUtils.class})
public class FilesystemCephAdapterImplTest {

	/** The api. */
	private static S3Mock api;

	/** The client. */
	private AmazonS3 client;

	/** The check enrolment id. */
	private String checkEnrolmentId;

	/** The file extension. */
	private String fileExtension;

	/** The dfs adapter. */

	private CephAdapterImpl dfsAdapter;

	@Autowired
	private Environment env = mock(Environment.class);

	/** The Constant FAILURE_ENROLMENT_ID. */
	private static final String FAILURE_ENROLMENT_ID = "1234";

	@Mock
	private ConnectionUtils connectionUtil;


	@BeforeClass
	public static void init() throws IOException {
		api = new S3Mock.Builder().withPort(6001).withInMemoryBackend().build();
		api.start();
	}

	/**
	 * This method sets up the required configuration before execution of test
	 * cases.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void setup() throws IOException {

		this.checkEnrolmentId = env.getProperty("mosip.kernel.fsadapter.ceph.test.check.enrolment.id");
		this.fileExtension = env.getProperty("mosip.kernel.fsadapter.ceph.test.file.extension");
		
		EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:6001", "us-west-2");
		client = AmazonS3ClientBuilder.standard().withPathStyleAccessEnabled(true).withEndpointConfiguration(endpoint)
				.withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials())).build();

		when(connectionUtil.getConnection()).thenReturn(client);
		dfsAdapter = new CephAdapterImpl(connectionUtil);
		// Putting a file to mocked ceph instance

		ClassLoader classLoader = getClass().getClassLoader();
		String filePath = classLoader.getResource(checkEnrolmentId + fileExtension).getFile();
		File packet = new File(filePath);
		dfsAdapter.storePacket(this.checkEnrolmentId, packet);
	}

	/**
	 * This method tests uploading of a packet to DFS.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testUploadPacketSuccess() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String filePath = classLoader.getResource(checkEnrolmentId + fileExtension).getFile();
		File packet = new File(filePath);
		boolean result = this.dfsAdapter.storePacket(this.checkEnrolmentId, packet);
		assertEquals("Successfully uploaded packet as filepath to DFS .", true, result);
	}

	/**
	 * This method tests getting a packet successfully from DFS.
	 *
	 * @return the packet success test
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testGetPacketSuccess() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String filePath = classLoader.getResource(checkEnrolmentId + fileExtension).getFile();
		InputStream packet = dfsAdapter.getPacket(checkEnrolmentId);
		ZipInputStream zis = new ZipInputStream(packet);
		ZipEntry ze = zis.getNextEntry();
		int actualFileCount = 0;
		while (ze != null) {
			actualFileCount++;
			ze = zis.getNextEntry();
		}
		ZipFile actualZipFile = new ZipFile(filePath);
		Enumeration<? extends ZipEntry> entries = actualZipFile.entries();
		int expectedFileCount = 0;
		while (entries.hasMoreElements()) {
			expectedFileCount++;
			entries.nextElement();
		}
		assertEquals("Should get packet for input registration id.", expectedFileCount, actualFileCount);
		actualZipFile.close();
	}

	/**
	 * This method tests getting a packet which is not present in DFS.
	 *
	 * @return the packet failure test
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test(expected = FSAdapterException.class)
	public void testGetPacketFailure() throws IOException {
		this.dfsAdapter.unpackPacket(FAILURE_ENROLMENT_ID);
	}

	/**
	 * This method checks unpacking of a packet in DFS.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testUnpackPacketFailure() throws IOException {
		this.dfsAdapter.unpackPacket(checkEnrolmentId);
		int noOfextractedFiles = this.client.listObjects(this.checkEnrolmentId).getObjectSummaries().size();
		assertEquals("Check the count of extracted file.", 5, noOfextractedFiles);
		// check file exists test
		boolean findResult = this.dfsAdapter.checkFileExistence(checkEnrolmentId, PacketFiles.BIOMETRIC.name());
		assertEquals("To check file exist or not by registration id.", true, findResult);
		// Get file test
		InputStream file = this.dfsAdapter.getFile(checkEnrolmentId, PacketFiles.DEMOGRAPHIC.name());
		assertNotNull("Get file as inpustream by registration id .", file);
		// Delete file test
		boolean result = this.dfsAdapter.deleteFile(checkEnrolmentId, PacketFiles.DEMOGRAPHIC.name());
		assertEquals("Delete file by registration id .", true, result);
	}

	/**
	 * This method checks fetching a file from a packet that is not present in DFS.
	 *
	 * @return the file failure test
	 */
	@Test(expected = FSAdapterException.class)
	public void testGetFileFailure() {
		this.dfsAdapter.getFile(FAILURE_ENROLMENT_ID, PacketFiles.DEMOGRAPHIC.name());
	}

	/**
	 * This method checks deleting a packet from DFS and later fetching it.
	 */
	@Test(expected = FSAdapterException.class)
	public void testDeletePacketFailure() {
		this.dfsAdapter.deletePacket(checkEnrolmentId);
		this.dfsAdapter.getPacket(checkEnrolmentId);
	}

	/**
	 * Store packet input stream success test.
	 */
	@Test
	public void testStorePacketInputStreamSuccess() {
		InputStream packet = dfsAdapter.getPacket(checkEnrolmentId);
		boolean result = this.dfsAdapter.storePacket(this.checkEnrolmentId, packet);
		assertEquals("Successfully uploaded packet as inputstream to DFS.", true, result);
	}

	/**
	 * Store packet connection unavailable exception test.
	 */
	@Test(expected = FSAdapterException.class)
	public void testStorePacketConnectionUnavailableException() {
		EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:6002", "us-west-2");
		AmazonS3 client1 = AmazonS3ClientBuilder.standard().withPathStyleAccessEnabled(true)
				.withEndpointConfiguration(endpoint).withCredentials(null).build();

		when(connectionUtil.getConnection()).thenReturn(client1);
		dfsAdapter = new CephAdapterImpl(connectionUtil);
		InputStream packet = new InputStream() {
			@Override
			public int read() throws IOException {
				return 0;
			}
		};
		this.dfsAdapter.storePacket(this.checkEnrolmentId, packet);
	}

	/**
	 * Store packet invalid connection parameter exception test.
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = FSAdapterException.class)
	public void testStorePacketInvalidConnectionParameterException() {

		when(connectionUtil.getConnection()).thenThrow(FSAdapterException.class);
		InputStream packet = new InputStream() {
			@Override
			public int read() throws IOException {
				return 0;
			}
		};
		dfsAdapter = new CephAdapterImpl(connectionUtil);
		this.dfsAdapter.storePacket(this.checkEnrolmentId, packet);
	}

	@Test
	public void fileExistenceFailureTest() {
		boolean result = this.dfsAdapter.checkFileExistence("ThisShouldNotExist", PacketFiles.BIOMETRIC.name());
		assertEquals(false, result);
	}

	/**
	 * This method destroys the dummy DFS connection.
	 */
	@AfterClass
	public static void destroy() {
		api.stop();
	}

}