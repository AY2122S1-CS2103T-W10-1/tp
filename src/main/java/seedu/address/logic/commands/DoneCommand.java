package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_INDEX;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * Marks task(s) identified using its displayed index in the task list as done.
 */
public class DoneCommand extends Command {

    public static final String COMMAND_WORD = "donetask";
    public static final String MESSAGE_SUCCESS = "Marked %1$d %2$s of %3$s as done.";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Marks the task, specified by the TASKINDEX, from person"
            + "identified by the index number used in the displayed person list as done.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + PREFIX_TASK_INDEX + "TaskIndex\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TASK_INDEX + " 2";

    public static final String DESCRIPTION = "Marks the task(s), specified by the TASK_INDEX, "
            + "from person specified by the INDEX as done";

    private final Index targetPersonIndex;
    private final List<Index> targetTaskIndexes;

    /**
     * Constructor for a DoneCommand to mark a task of a person as done.
     *
     * @param targetPersonIndex The Index of the target person.
     * @param targetTaskIndexes The Index of the target Task that belongs to target person.
     */
    public DoneCommand(Index targetPersonIndex, List<Index> targetTaskIndexes) {
        requireAllNonNull(targetPersonIndex, targetTaskIndexes);
        this.targetPersonIndex = targetPersonIndex;
        this.targetTaskIndexes = targetTaskIndexes;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetPersonIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(targetPersonIndex.getZeroBased());

        //Make new copy for defensive programming.
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(personToEdit.getTasks());
        List<Index> copyOfIndexList = new ArrayList<>();
        copyOfIndexList.addAll(targetTaskIndexes);

        Collections.sort(copyOfIndexList, Collections.reverseOrder());

        for (Index targetTaskIndex : targetTaskIndexes) {
            if (targetTaskIndex.getZeroBased() >= tasks.size()) {
                throw new CommandException(String.format(Messages.MESSAGE_INVALID_TASK, personToEdit.getName()));
            }
        }

        for (Index targetTaskIndex : copyOfIndexList) {
            Task taskDone = tasks.get(targetTaskIndex.getZeroBased());
            taskDone.setDone();
        }

        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getTags(), tasks, personToEdit.getDescription());

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(generateSuccessMessage(editedPerson, targetTaskIndexes.size()));
    }

    public String getCommand() {
        return COMMAND_WORD;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Generates a command execution success message based on
     * the task removed.
     * {@code personToEdit}.
     */
    private String generateSuccessMessage(Person personToEdit, int amount) {
        String taskOrTasks = StringUtil.singularOrPlural("task", amount);
        return String.format(MESSAGE_SUCCESS, amount, taskOrTasks, personToEdit.getName());
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DoneCommand // instanceof handles nulls
                && targetPersonIndex.equals(((DoneCommand) other).targetPersonIndex)
                && targetTaskIndexes.equals(((DoneCommand) other).targetTaskIndexes)); // state check
    }

}
