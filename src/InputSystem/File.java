package InputSystem;

import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * The {@link File} class provides static methods for the creation, copying, deletion, moving, and opening of a single
 * file, and aids in the creation of {@link FileInputStream}/{@link FileOutputStream} objects.
 * @implNote If there is some performance issue, please tell me. After that, I will fix this.
 */
public final class File
{
    /**
     * This class should not be instantiated since it is designed to provide static methods only.
     * */
    private File(){}

    /**
     * Copies an existing file to a new file if it doesn't exist, otherwise, do nothing.
     * @param sourceFileName The file to copy.
     * @param destFileName The name of the destination file. This cannot be a directory.
     * @exception IOException If an I/O error occurs.
     * @exception NullPointerException If {@code sourceFileName}/{@code destFileName} is null.
     * */
    public static void copy(String sourceFileName, String destFileName) throws IOException
    {
        validateSourceDestinationFileName(sourceFileName, destFileName);
        copy(sourceFileName, destFileName, false);
    }

    /**
     * Copies an existing file to a new file. Overwriting a file of the same name is allowed.
     * @param sourceFileName The file to copy.
     * @param destFileName The name of the destination file. This cannot be a directory.
     * @param overwrite true if the destination file can be overwritten; otherwise, false.
     * @exception IOException If the path string cannot be converted to a {@link Path}, an I/O error occurs.
     * @exception NullPointerException If {@code sourceFileName}/{@code destFileName} is null.
     * */
    public static void copy(String sourceFileName, String destFileName, boolean overwrite) throws IOException
    {
        validateSourceDestinationFileName(sourceFileName, destFileName);

        // Do nothing if the destination file exists and overwriting a file is not allowed.
        Path destPath = Paths.get(destFileName);
        if ((!destPath.toFile().exists()) || overwrite)
        {
            Path sourcePath = Paths.get(sourceFileName);
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    /**
     * Deletes the specified file.
     * @param path The name of the file to be deleted. Wildcard characters are not supported.
     * @return <code>true</code> if and only if the file or directory is successfully deleted, otherwise,
     * <code>false</code>.
     * @exception NullPointerException If {@code path} is null.
     * */
    public static boolean delete(String path)
    {
        validatePath(path);
        return new java.io.File(path).delete();
    }

    /**
     * Determines whether the specified file exists.
     * @param path The file to check.
     * @return true if the caller has the required permissions and "path" contains the name of an existing file;
     * otherwise, false.
     * @exception NullPointerException If the given {@code path} is null.
     * */
    public static boolean exists(String path)
    {
        validatePath(path);
        return new java.io.File(path).exists();
    }

    /**
     * Opens an existing file for reading.
     * @param path The file to be opened for reading.
     * @return A read-only {@link FileInputStream} on the specified path.
     * @exception FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path} is null.
     * */
    @NotNull
    @Contract("_ -> new")
    public static FileInputStream openRead(String path) throws FileNotFoundException
    {
        validatePath(path);
        return new FileInputStream(path);
    }

    /**
     * Opens an existing file or creates a new file for writing.
     * @param path The file to be opened for writing.
     * @return An unshared {@link FileOutputStream} object on the specified path with write access.
     * @exception FileNotFoundException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason.
     * @exception NullPointerException If {@code path} is null.
     * */
    @NotNull
    @Contract("_ -> new")
    public static FileOutputStream openWrite(String path) throws FileNotFoundException
    {
        validatePath(path);
        return new FileOutputStream(path);
    }

    /**
     * Opens an existing file or creates a new file for appending new contents.
     * @param path The file to be opened for appending.
     * @return An unshared {@link FileOutputStream} object on the specified path with write access.
     * @exception FileNotFoundException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason.
     * @exception NullPointerException If @code path} is null.
     * */
    @NotNull
    @Contract("_ -> new")
    public static FileOutputStream openAppend(String path) throws FileNotFoundException
    {
        validatePath(path);
        return new FileOutputStream(path, true);
    }

    /**
     * Opens a text file with UTF-8 encoding, reads all contents of the file, and then closes the file.
     * @param path The file to open for reading.
     * @return A string containing all contents of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path} is null.
     * */
    public static String readAllText(String path) throws IOException
    {
        validatePath(path);
        return readAllText(path, StandardCharsets.UTF_8);
    }

    /**
     * Opens a text file, reads all contents of the file with the specified encoding, and then closes the file.
     * @param path The file to open for reading.
     * @param charset The encoding applied to the contents of the file.
     * @return A string containing all contents of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path}/{@code charset} is null.
     * */
    public static String readAllText(String path, Charset charset) throws IOException
    {
        validatePath(path);
        validateCharset(charset);

        Scanner input = new Scanner(Paths.get(path), charset);
        StringBuilder contents = new StringBuilder();
        while (input.hasNextLine())
        {
            contents.append(input.nextLine());
            contents.append("\n");
        }
        input.close();

        // Discard the last appended "\n".
        return contents.substring(0, contents.length() - 1);
    }

    /**
     * Creates a new file with UTF-8 encoding, writes the specified string to the file, and then closes the file. If th
     * target file already exists, it is overwritten. If the file doesn't exist, it will be created first, then contents
     * will be written into the file.
     * @param path The file to write to.
     * @param contents The string to write to the file.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents} is null.
     * */
    public static void writeAllText(String path, String contents) throws IOException
    {
        validatePathContents(path, contents);
        writeAllText(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new file, writes the specified string to the file, and then closes the file. If the target file already
     * exists, it is overwritten. If the file doesn't exist, it will be created first, then contents will be written
     * into the file.
     * @param path The file to write to.
     * @param contents The string to write to the file.
     * @param charset The encoding to apply to the string.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents}/{code charset} is null.
     * */
    public static void writeAllText(String path, String contents, Charset charset) throws IOException
    {
        validatePathContents(path, contents);
        validateCharset(charset);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), charset);
        writer.write(contents);
        writer.flush();
        writer.close();
    }

    /**
     * Opens a binary file, reads the contents of the file into a byte array, and then closes the file.
     * @param path The file to open for reading.
     * @return A byte array containing the contents of the file.
     * @exception IOException If the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path} is null.
     * */
    @NotNull
    public static byte[] readAllBytes(String path) throws IOException
    {
        validatePath(path);
        return readAllText(path).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a new file, writes the specified byte array to the file, and then closes the file. If the target file
     * already exists, it is overwritten.
     * @param path The file to write to.
     * @param bytes The bytes to write to the file.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code bytes} is null.
     * */
    public static void writeAllBytes(String path, byte[] bytes) throws IOException
    {
        validatePath(path);
        if (bytes == null)
            throw new NullPointerException("bytes cannot be null.");

        FileOutputStream output = new FileOutputStream(path);
        output.write(bytes);
        output.flush();
        output.close();
    }

    /**
     * Opens a file, reads all lines of the file with UTF-8 encoding, and then closes the file.
     * @param path The file to open for reading.
     * @return A string array containing all lines of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path} is null.
     * */
    public static String[] readAllLines(String path) throws IOException
    {
        validatePath(path);
        return readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Opens a file, reads all lines of the file with the specified encoding, and then closes the file.
     * @param path The file to open for reading.
     * @param charset The encoding applied to the contents of the file.
     * @return A string array containing all lines of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path}/{@code charset} is null.
     * */
    public static String[] readAllLines(String path, Charset charset) throws IOException
    {
        validatePath(path);
        validateCharset(charset);

        Scanner input = new Scanner(Paths.get(path), charset);
        LinkedList<String> inputBuffer = new LinkedList<>();
        while (input.hasNextLine())
            inputBuffer.addLast(input.nextLine());
        input.close();

        String[] contents = new String[inputBuffer.size()];
        int i = 0;
        for (String line : inputBuffer)
            contents[i++] = line;

        return contents;
    }

    /**
     * Read the lines of a file that has UTF-8 encoding.
     * @param path The file to read.
     * @return A string array containing all lines of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path} is null.
     * */
    public static Iterable<String> readLines(String path) throws IOException
    {
        validatePath(path);
        return readLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Read the lines of a file that has a specified encoding.
     * @param path The file to read.'
     * @param charset The encoding that is applied to the contents of the file.
     * @return A string array containing all lines of the file.
     * @exception IOException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     * @exception NullPointerException If {@code path}/{@code charset} is null.
     * */
    public static Iterable<String> readLines(String path, Charset charset) throws IOException
    {
        validatePath(path);
        validateCharset(charset);

        Scanner input = new Scanner(Paths.get(path), charset);
        LinkedList<String> contents = new LinkedList<>();
        while (input.hasNextLine())
            contents.addLast(input.nextLine());
        input.close();

        return contents;
    }

    /**
     * Writes specified contents to the specified file with charset UTF-8. If the file doesn't exist, it is created.
     * @param path The file to write the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to write to the file.
     * @exception IOException if the path exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents} is null.
     * */
    public static void writeAllLines(String path, String[] contents) throws IOException
    {
        validatePathContents(path, contents);
        writeAllLines(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Writes specified contents to the specified file with given charset. If the file doesn't exist, it is created.
     * @param path The file to write the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to write to the file. The file is created if it doesn't already exist.
     * @param charset The character encoding to use.
     * @exception IOException if the path exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents}/{@code charset} is null.
     * */
    public static void writeAllLines(String path, String[] contents, Charset charset) throws IOException
    {
        validatePathContents(path, contents);
        validateCharset(charset);

        StringBuilder contentsToWrite = new StringBuilder();
        for (String line : contents)
        {
            contentsToWrite.append(line);
            contentsToWrite.append(System.lineSeparator());
        }

        writeAllText(path, contentsToWrite.substring(0, contentsToWrite.length() - System.lineSeparator().length()), charset);
    }

    /**
     * Appends the specified string to the file, creating the file if it does not already exist.
     * @param path The file to append the specified string to.
     * @param contents The string to append to the file.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents} is null.
     * */
    public static void appendAllText(String path, String contents) throws IOException
    {
        validatePathContents(path, contents);
        appendAllText(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Appends the specified string to the file, creating the file if it does not already exist.
     * @param path The file to append the specified string to.
     * @param contents The string to append to the file.
     * @param charset The character encoding to use.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents}/{@code charset} is null.
     * */
    public static void appendAllText(String path, String contents, Charset charset) throws IOException
    {
        validatePathContents(path, contents);
        validateCharset(charset);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path, true), charset);
        writer.write(contents);
        writer.flush();
        writer.close();
    }

    /**
     * Append specified contents to the specified file with charset UTF-8. If the file doesn't exist, it will be created.
     * @param path The file to append the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to append to the file.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents} is null.
     * */
    public static void appendAllLines(String path, Iterable<String> contents) throws IOException
    {
        validatePathContents(path, contents);
        appendAllLines(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Append specified contents to the specified file with given charset. If the file doesn't exist, it will be created.
     * @param path The file to append the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to append to the file.
     * @param charset The character encoding to use.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents}/{@code charset} is null.
     * */
    public static void appendAllLines(String path, Iterable<String> contents, Charset charset) throws IOException
    {
        validatePathContents(path, contents);
        validateCharset(charset);

        StringBuilder contentsToWrite = new StringBuilder();
        for (String line : contents)
        {
            contentsToWrite.append(System.lineSeparator());
            contentsToWrite.append(line);
        }

        // Clip the line separator before writing contents to file.
        appendAllText(path, contentsToWrite.toString(), charset);
    }

    /**
     * Append specified contents to the specified file with charset UTF-8. If the file doesn't exist, it will be created.
     * @param path The file to append the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to append to the file.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents} is null.
     * */
    public static void appendAllLines(String path, String[] contents) throws IOException
    {
        validatePathContents(path, contents);
        appendAllLines(path, contents, StandardCharsets.UTF_8);
    }

    /**
     * Append specified contents to the specified file with given charset. If the file doesn't exist, it will be created.
     * @param path The file to append the lines to. The file is created if it doesn't already exist.
     * @param contents The lines to append to the file.
     * @param charset The character encoding to use.
     * @exception IOException if the file exists but is a directory rather than a regular file, does not
     * exist but cannot be created, or cannot be opened for any other reason. Or an I/O error occurs when writing
     * contents to the file.
     * @exception NullPointerException If {@code path}/{@code contents}/{@code charset} is null.
     * */
    public static void appendAllLines(String path, String[] contents, Charset charset) throws IOException
    {
        validatePathContents(path, contents);
        validateCharset(charset);

        StringBuilder contentsToWrite = new StringBuilder();
        for (String line : contents)
        {
            contentsToWrite.append(System.lineSeparator());
            contentsToWrite.append(line);
        }

        // Clip the line separator before writing contents to file.
        appendAllText(path, contentsToWrite.toString(), charset);
    }

    /**
     * Moves an existing file to a new path if it doesn't exist, otherwise, do nothing.
     * @param sourceFileName The file to move.
     * @param destFileName The name of the destination file. This cannot be a directory.
     * @exception IOException If an I/O error occurs.
     * @exception NullPointerException If {@code sourceFileName}/{@code destFileName} is null.
     * */
    public static void move(String sourceFileName, String destFileName) throws IOException
    {
        validateSourceDestinationFileName(sourceFileName, destFileName);
        Files.move(Paths.get(sourceFileName), Paths.get(destFileName), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Replaces the contents of a specified file with the contents of another file, deleting the original file, and
     * creating a backup of the replaced file.
     * @param sourceFileName The file to move.
     * @param destFileName The name of the destination file. This cannot be a directory.
     * @param destBackupFileName The name of the backup file.
     * @exception IOException If an I/O error occurs.
     * @exception NullPointerException If {@code sourceFileName}/{@code destFileName}/{@code destBackupFileName} is
     * null.
     * */
    @Contract("null, _, _ -> fail; !null, null, _ -> fail; !null, !null, null -> fail")
    public static void replace(String sourceFileName, String destFileName, String destBackupFileName) throws IOException
    {
        if (sourceFileName == null)
            throw new NullPointerException("Source file name cannot be null.");
        if (destFileName == null)
            throw new NullPointerException("Destination file name cannot be null.");
        if (destBackupFileName == null)
            throw new NullPointerException("Destination backup file name cannot be null.");

        Files.move(Paths.get(destFileName), Paths.get(destBackupFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.move(Paths.get(sourceFileName), Paths.get(destFileName), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Renames the source file denoted by the new pathname.
     * @param sourceFileName The name of the source file.
     * @param destFileName The new pathname for the named file.
     * @exception NullPointerException If {@code sourceFileName}/{@code destFileName} is null.
     * */
    public static void rename(String sourceFileName, String destFileName)
    {
        validateSourceDestinationFileName(sourceFileName, destFileName);
        new java.io.File(sourceFileName).renameTo(new java.io.File(destFileName));
    }

    @Contract("null, _ -> fail; !null, null -> fail")
    private static void validateSourceDestinationFileName(String sourceFileName, String destFileName)
    {
        if (sourceFileName == null)
            throw new NullPointerException("Parameter \"sourceFileName\" cannot be null.");
        if (destFileName == null)
            throw new NullPointerException("Parameter \"destFileName\" cannot be null.");
    }

    @Contract("null -> fail")
    private static void validatePath(String path)
    {
        if (path == null)
            throw new NullPointerException("Parameter \"path\" cannot be null.");
    }

    @Contract("null, _ -> fail; !null, null -> fail")
    private static void validatePathContents(String path, String[] contents)
    {
        if (path == null)
            throw new NullPointerException("Parameter \"path\" cannot be null.");
        if (contents == null)
            throw new NullPointerException("Parameter \"contents\" cannot be null.");
    }

    @Contract("null, _ -> fail; !null, null -> fail")
    private static void validatePathContents(String path, Iterable<String> contents)
    {
        if (path == null)
            throw new NullPointerException("Parameter \"path\" cannot be null.");
        if (contents == null)
            throw new NullPointerException("Parameter \"contents\" cannot be null.");
    }

    @Contract("null, _ -> fail; !null, null -> fail")
    private static void validatePathContents(String path, String contents)
    {
        if (path == null)
            throw new NullPointerException("Parameter \"path\" cannot be null.");
        if (contents == null)
            throw new NullPointerException("Parameter \"contents\" cannot be null.");
    }

    @Contract("null -> fail")
    private static void validateCharset(Charset charset)
    {
        if (charset == null)
            throw new NullPointerException("Parameter \"charset\" cannot be null.");
    }
}
