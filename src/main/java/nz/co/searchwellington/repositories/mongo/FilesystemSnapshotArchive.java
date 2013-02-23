package nz.co.searchwellington.repositories.mongo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Snapshot;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class FilesystemSnapshotArchive implements SnapshotArchive {
	
	private static Logger log = Logger.getLogger(FilesystemSnapshotArchive.class);

	private static final DateTimeFormatter DATE_TIME_NO_MILLIS = ISODateTimeFormat.dateTimeNoMillis();
	private static final String BODY_HTML = "body.html";
	
	private String archiveRootPath = "/tmp";

	@Override
	public Snapshot getLatestFor(String url) {
		File snapshotFolderForUrl = new File(folderPathForUrl(url));
		
		final List<File> folders = new ArrayList<File>(FileUtils.listFilesAndDirs(snapshotFolderForUrl, FalseFileFilter.INSTANCE , DirectoryFileFilter.INSTANCE));
		Collections.sort(folders);
		Collections.reverse(folders);
		
		List<File> dateFolders = new ArrayList<File>();
		for (File folder : folders) {
			if (folder.getName().matches("\\d\\d\\d\\d-.*")) {
				dateFolders.add(folder);
			}
		}
        
		if (!dateFolders.isEmpty()) {
			File latestSnapshotFolder = dateFolders.get(0);
			System.out.println(latestSnapshotFolder.getAbsolutePath());
			File bodyFile = new File(latestSnapshotFolder.getAbsoluteFile() + "/" + BODY_HTML);
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(bodyFile);
				final String body = IOUtils.toString(fileInputStream);
				IOUtils.closeQuietly(fileInputStream);
				
				final String[] folderComponents = latestSnapshotFolder.getAbsolutePath().split("/");
				String folderDateString = folderComponents[folderComponents.length-1];
				System.out.println(folderDateString);
				
				Date date = parseDate(folderDateString);
				return new Snapshot(url, date, body);
				
			} catch (FileNotFoundException e) {
				log.error(e);
			} catch (IOException e) {
				log.error(e);
			}
		}		
		return null;
	}

	@Override
	public void put(Snapshot snapshot) {
		final String snapshotFolderPath = folderPathForUrl(snapshot.getUrl()) + "/" + printDate(snapshot);
		File snapshotFolder = new File(snapshotFolderPath);
		if (!snapshotFolder.exists()) {
			snapshotFolder.mkdirs();
		}
		
		File snapshotFile = new File(snapshotFolderPath + "/" + BODY_HTML);		
		try {
			log.info("Writing snapshot to file: " + snapshotFile.getAbsolutePath());
			FileOutputStream fileOutputStream = new FileOutputStream(snapshotFile);
			IOUtils.write(snapshot.getBody().getBytes(), fileOutputStream);
			IOUtils.closeQuietly(fileOutputStream);
			
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private String printDate(Snapshot snapshot) {
		return DATE_TIME_NO_MILLIS.print(snapshot.getDate().getTime());
	}

	private Date parseDate(String folderDateString) {
		return DATE_TIME_NO_MILLIS.parseDateTime(folderDateString).toDate();
	}

	private String folderPathForUrl(String url) {
		return archiveRootPath + "/" + getFolderNameForUrl(url);
	}
	
	private String getFolderNameForUrl(String url) {
		return Base64.encodeBase64String(url.getBytes());
	}
	
}
