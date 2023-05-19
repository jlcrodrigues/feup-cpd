package server.concurrent;

import server.store.Store;
import server.utils.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * Defines an api to interact with the user database. <br>
 * Makes use of a read-write lock to allow concurrent reads and exclusive writes.
 */
public class Database {
    private ReentrantReadWriteLock lock;
    private final String fileName = "src/server/users.txt";

    public Database() {
        lock = new ReentrantReadWriteLock();
    }

    /**
     * Retrieve the hashed user password and elo from storage.
     *
     * @param username Username to search for.
     * @return List with user password hashed with sha-256 and elo or null if the user does not exist.
     */
    public String[] getUserInfo(String username) {
        lock.readLock().lock();
        try {
            File file = new File(fileName);

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] fields = line.split(",");

                if (fields[0].equals(username)) {
                    return new String[]{fields[1], fields[2]};
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            Store.getStore().log(Level.SEVERE, "File not found: " + fileName);
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public void registerUser(Map<String, Object> args) throws IOException, NoSuchAlgorithmException {
        lock.writeLock().lock();
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    (args.get("password").toString()).getBytes(StandardCharsets.UTF_8));

            bufferedWriter.newLine();
            bufferedWriter.write(args.get("username") + "," + Utils.bytesToHex(encodedHash) + ",1000");

            bufferedWriter.close();
            fileWriter.close();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Update the elo of the users in the database. <br>
     * This is done with a map to because updating multiple users at once is way faster.
     * @param elo Map with username as key and elo as value.
     */
    public void updateElo(Map<String, Integer> elo) {
        lock.writeLock().lock();
        try {
            File file = new File(fileName);
            File tempFile = new File("src/server/temp.txt");

            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String[] fields = currentLine.split(",");

                if (elo.containsKey(fields[0])) {
                    writer.write(fields[0] + "," + fields[1] + "," + elo.get(fields[0]));
                } else {
                    writer.write(currentLine);
                }
                writer.newLine();
            }

            reader.close();
            writer.close();

            file.delete();
            tempFile.renameTo(file);
        } catch (IOException e) {
            Store.getStore().log(Level.SEVERE, "Error updating elo");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
