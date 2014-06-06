package pl.touk.sputnik.review;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.touk.sputnik.connector.gerrit.json.ReviewFileComment;
import pl.touk.sputnik.connector.gerrit.json.ReviewInput;
import pl.touk.sputnik.connector.gerrit.json.ReviewLineComment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Review {
    /* Source, severity, message, e.g. [Checkstyle] Info: This is bad */
    private static final String COMMENT_FORMAT = "[%s] %s: %s";
    private final List<ReviewFile> files;
    private int totalViolationsCount = 0;

    public Review(List<ReviewFile> files) {
        this.files = files;
    }

    @NotNull
    public List<File> getIOFiles() {
        return Lists.transform(files, new Review.ReviewFileFileFunction());
    }

    @NotNull
    public List<String> getIOFilenames() {
        return Lists.transform(files, new Review.ReviewFileFilenameFunction());
    }

    @NotNull
    public List<String> getJavaClassNames() {
        return Lists.transform(files, new Review.ReviewFileJavaFileNameFunction());
    }

    @NotNull
    public List<String> getBuildDirs() {
        return Lists.transform(files, new ReviewFileBuildDirFunction());
    }

    @NotNull
    public List<String> getSourceDirs() {
        return Lists.transform(files, new ReviewFileSourceDirFunction());
    }

    @NotNull
    public ReviewInput toReviewInput() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.message = "Total " + totalViolationsCount + " violations found";
        reviewInput.setLabelToPlusOne();
        for (ReviewFile file : files) {
            List<ReviewFileComment> comments = new ArrayList<ReviewFileComment>();
            for (Comment comment : file.getComments()) {
                comments.add(new ReviewLineComment(comment.getLine(), comment.getMessage()));
            }
            reviewInput.comments.put(file.getGerritFilename(), comments);
        }

        return reviewInput;
    }

    public void add(@NotNull String source, @NotNull ReviewResult reviewResult) {
        for(Violation violation : reviewResult.getViolations()) {
            addError(source, violation);
        }
    }

    public void addError(String source, Violation violation) {
        for (ReviewFile file : files) {
            if (file.getGerritFilename().equals(violation.getFilenameOrJavaClassName()) ||
                    file.getIoFile().getAbsolutePath().equals(violation.getFilenameOrJavaClassName()) ||
                    file.getJavaClassName().equals(violation.getFilenameOrJavaClassName())) {
                addError(file, source, violation.getLine(), violation.getMessage(), violation.getSeverity());
                totalViolationsCount++;
                return;
            }
        }
        log.warn("Filename or Java class {} was not found in current review", violation.getFilenameOrJavaClassName());
    }

    private void addError(@NotNull ReviewFile reviewFile, @NotNull String source, int line, @Nullable String message, Severity severity) {
        reviewFile.getComments().add(new Comment(line, String.format(COMMENT_FORMAT, source, severity, message)));
    }

    private static class ReviewFileFileFunction implements Function<ReviewFile, File> {
        ReviewFileFileFunction() { }

        @Override
        public File apply(ReviewFile from) {
            return from.getIoFile();
        }
    }

    private static class ReviewFileFilenameFunction implements Function<ReviewFile, String> {
        ReviewFileFilenameFunction() { }

        @Override
        public String apply(ReviewFile from) {
            return from.getGerritFilename();
        }
    }

    private static class ReviewFileJavaFileNameFunction implements Function<ReviewFile, String> {
        ReviewFileJavaFileNameFunction() { }

        @Override
        public String apply(ReviewFile from) {
            return from.getJavaClassName();
        }
    }

    private static class ReviewFileBuildDirFunction implements Function<ReviewFile, String> {
        ReviewFileBuildDirFunction() { }

        @Override
        public String apply(ReviewFile from) {
            return from.getBuildDir();
        }
    }

    private static class ReviewFileSourceDirFunction implements Function<ReviewFile, String> {
        ReviewFileSourceDirFunction() { }

        @Override
        public String apply(ReviewFile from) {
            return from.getSourceDir();
        }
    }
}
