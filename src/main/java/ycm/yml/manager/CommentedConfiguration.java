package ycm.yml.manager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * I DID NOT WRITE THE ORIGINAL FILE
 * I cannot find the original file, so I just copied what I found, so I can modify it how I want
 * This exact file is in so many other projects, so I really don't know who the owner actually is
 *
 * @author Apple (amp7368)
 * @author dumptruckman
 * might be this person maybe? https://github.com/dumptruckman, but this also might not be thiers?
 * @author Lukas Mansour (Articdive)
 * might be this person maybe? https://github.com/Articdive, but this also might not be theirs?
 */
public class CommentedConfiguration extends YamlConfiguration {
    private final Map<String, String> commentsNewLine = new HashMap<>();
    private final Map<String, String> commentsInLine = new HashMap<>();
    private final File file;

    public CommentedConfiguration(File file) {
        this.file = file;
    }

    public CommentedConfiguration() {
        this.file = null;
    }

    public void load() throws IOException, InvalidConfigurationException {
        this.load(file);
    }

    @Override
    public void save(File file) throws IOException {
        super.save(file);
        this.addCommentsToFile(file);
    }

    @Override
    public void save(String filename) throws IOException {
        File file = new File(filename);
        super.save(file);
        this.addCommentsToFile(file);
    }

    public void save() throws IOException {
        super.save(file);
        this.addCommentsToFile(file);
    }

    private void addCommentsToFile(File fileForComments) throws IOException {
        // if there's comments to add, and it saved fine, we need to add comments

        if (commentsInLine.isEmpty() && commentsNewLine.isEmpty()) return;

        // the temporary file for the config to write the new file to
        File tempOutputFile = new File(fileForComments.getAbsolutePath() + "temp");
        BufferedWriter tempOutputWriter = new BufferedWriter(new FileWriter(tempOutputFile));

        // the file we just wrote to
        BufferedReader inputFileReader = new BufferedReader(new FileReader(fileForComments));

        // This holds the current path the lines are at in the config
        String currentPath = "";
        // This flags if the line is a node or unknown text.
        boolean node;
        // The depth of the path. (number of words separated by periods minus 1)
        int depth = 0;
        // Whether we've not hit the root content yet
        boolean isBeginning = true;

        // the current line we're reading
        String line;
        // Loop through the config lines
        while ((line = inputFileReader.readLine()) != null) {
            // skip comments at the beginning because of a bug with bukkit according to previous owners
            if (isBeginning && line.trim().startsWith("#"))
                continue;
            isBeginning = false;
            // If the line is a node (and not something like a list value)
            if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {

                // This is a node so flag it as one
                node = true;

                // Grab the index of the end of the node name
                int index;
                index = line.indexOf(": ");
                if (index < 0) {
                    index = line.length() - 1;
                }
                // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                if (currentPath.isEmpty()) {
                    currentPath = line.substring(0, index);
                } else {
                    // Calculate the whitespace preceding the node name
                    int whiteSpace = 0;
                    for (int n = 0; n < line.length(); n++) {
                        if (line.charAt(n) == ' ') {
                            whiteSpace++;
                        } else {
                            break;
                        }
                    }
                    // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                    if (whiteSpace / 2 > depth) {
                        // Path is deeper.  Add a . and the node name
                        currentPath += "." + line.substring(whiteSpace, index);
                        depth++;
                    } else if (whiteSpace / 2 < depth) {
                        // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                        int newDepth = whiteSpace / 2;
                        for (int i = 0; i < depth - newDepth; i++) {
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                        }
                        // Grab the index of the final period
                        int lastIndex = currentPath.lastIndexOf(".");
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = "";
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath += ".";
                        }
                        // Add the new node name to the path
                        currentPath += line.substring(whiteSpace, index);
                        // Reset the depth
                        depth = newDepth;
                    } else {
                        // Path is same depth, replace the last path node name to the current node name
                        int lastIndex = currentPath.lastIndexOf(".");
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = "";
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath += ".";
                        }
                        //currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                        currentPath += line.substring(whiteSpace, index);

                    }

                }

            } else {
                node = false;
            }


            String commentInline = commentsInLine.get(currentPath);
            // if there's a commentInline for the current path, comment it
            if (commentInline != null) {
                // add the commentInLine at the end of the current line
                line = line + " # " + commentInline;
            }

            if (node) {
                // If there's a commentNewline for the current path, comment it
                String commentNewline = commentsNewLine.get(currentPath);

                if (commentNewline != null) {
                    // Add the commentNewline to the beginning of the current line
                    // and on the next line write the next commentNewline
                    line = commentNewline + System.lineSeparator() + line;
                }
            }
            // Add the (modified) line to the total config String
            tempOutputWriter.write(line);
            tempOutputWriter.write(System.lineSeparator());
        }
        tempOutputWriter.flush();
        tempOutputWriter.close();
        inputFileReader.close();
        File tempOldFile = new File(fileForComments.getAbsolutePath() + "temptemp");
        boolean renamed = fileForComments.renameTo(tempOldFile);
        if (tempOutputFile.renameTo(fileForComments)) {
            if (renamed) {
                tempOldFile.delete();
            }
        } else if (renamed) {
            tempOldFile.renameTo(fileForComments);
        }

    }

    public void addCommentNewline(String path, String commentLine, String... pathChildren) {
        addCommentNewline(path, new String[]{commentLine}, pathChildren);
    }

    /**
     * Adds a comment just before the specified path. The comment can be
     * multiple lines. An empty string will indicate a blank line.
     *
     * @param path         Configuration path to add comment.
     * @param commentLines Comments to add. One String per line.
     */
    public void addCommentNewline(String path, String[] commentLines, String... pathChildren) {
        path = path + String.join(".", pathChildren);
        StringBuilder commentstring = new StringBuilder();
        StringBuilder leadingSpaces = new StringBuilder();
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == '.') {
                leadingSpaces.append("  ");
            }
        }
        for (String line : commentLines) {
            line = line.isEmpty() ? " " : leadingSpaces + line;
            if (commentstring.length() != 0) {
                commentstring.append(System.lineSeparator());
            }
            commentstring.append("# ").append(line);
        }
        commentsNewLine.put(path, commentstring.toString());
    }

    /**
     * Appends a comment in the specified path.
     *
     * @param path    Configuration path to add the comment.
     * @param comment Comment to add.
     */
    public void addCommentInline(String path, String comment, String... pathChildren) {
        StringBuilder pathBuilder = new StringBuilder(path);
        for (String child : pathChildren)
            pathBuilder.append(".").append(child);
        path = pathBuilder.toString();
        commentsInLine.put(path, comment);
    }

    @Override
    public String saveToString() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(options().indent());
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setWidth(10000);
        YamlRepresenter yamlRepresenter = new YamlRepresenter();
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String dump = new Yaml(yamlRepresenter, dumperOptions).dump(getValues(false));


        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }

    public Map<String, String> getCommentsNewLine() {
        return new HashMap<>(commentsNewLine);
    }

    public Map<String, String> getCommentsInLine() {
        return new HashMap<>(commentsInLine);
    }

    public File getFile() {
        return file;
    }
}