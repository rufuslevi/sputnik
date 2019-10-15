package pl.touk.sputnik.connector.gerrit;

import com.google.common.base.Joiner;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import pl.touk.sputnik.review.Comment;
import pl.touk.sputnik.review.Review;
import pl.touk.sputnik.review.ReviewFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class ReviewInputBuilder {

    private final CommentFilter commentFilter;

    @NotNull
    public ReviewInput toReviewInput(@NotNull Review review) {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.message = Joiner.on(". ").join(review.getMessages());
        reviewInput.labels = new HashMap<String, Short>(review.getScores());
        reviewInput.comments = new HashMap<String, List<ReviewInput.CommentInput>>();
        for (ReviewFile file : review.getFiles()) {
            List<ReviewInput.CommentInput> comments = new ArrayList<ReviewInput.CommentInput>();
            for (Comment comment : file.getComments()) {
                 if (!(commentFilter.include(file.getReviewFilename(), comment.getLine()))) {
                    log.info("Comment excluded in file {}: line {}, message {}",
                            file.getReviewFilename(), comment.getLine(), comment.getMessage());
                    continue;
                }
                ReviewInput.CommentInput commentInput = new ReviewInput.CommentInput();
                commentInput.line = comment.getLine();
                commentInput.message = comment.getMessage();
                comments.add(commentInput);
            }
            reviewInput.comments.put(file.getReviewFilename(), comments);
        }

        return reviewInput;
    }
}
