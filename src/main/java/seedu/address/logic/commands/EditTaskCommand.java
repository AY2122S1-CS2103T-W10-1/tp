package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_VENUE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;
import seedu.address.model.task.TaskDate;
import seedu.address.model.task.TaskName;
import seedu.address.model.task.TaskTime;
import seedu.address.model.task.Venue;

/**
 * Edits the details of an existing task.
 */
public class EditTaskCommand extends Command {

    public static final String COMMAND_WORD = "edittask";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the task identified "
            + "by the index number used in the displayed person list and task number. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_TASK_INDEX + " TASK_INDEX "
            + "[" + PREFIX_TASK_DESCRIPTION + " TASK_NAME] "
            + "[" + PREFIX_TASK_DATE + " TASK_DATE] "
            + "[" + PREFIX_TASK_TIME + " TASK_TIME] "
            + "[" + PREFIX_TASK_VENUE + " TASK_ADDRESS] \n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_TASK_INDEX + "2 "
            + PREFIX_TASK_DESCRIPTION + "Assignment Discussion";

    public static final String DESCRIPTION = "Edits the details of the task identified";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Edited Task: %1$s ";
    public static final String MESSAGE_TASK_NOT_EDITED = "At least one field of task to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_TASK = "Task already exists.";
    public static final String MESSAGE_INVALID_TASK = "The size of %1$s's task list is not that big";

    private final Index targetPersonIndex;
    private final Index targetTaskIndex;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * Constructor for EditTaskCommand to edit a person's task.
     *
     * @param targetPersonIndex of the person in the filtered person list
     * @param targetTaskIndex of the task to edit
     */
    public EditTaskCommand(Index targetPersonIndex, Index targetTaskIndex, EditTaskDescriptor editTaskDescriptor) {
        requireNonNull(targetPersonIndex);
        requireNonNull(targetTaskIndex);
        requireNonNull(editTaskDescriptor);

        this.targetPersonIndex = targetPersonIndex;
        this.targetTaskIndex = targetTaskIndex;
        this.editTaskDescriptor = editTaskDescriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetPersonIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(targetPersonIndex.getZeroBased());
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(personToEdit.getTasks());

        if (targetTaskIndex.getZeroBased() >= tasks.size()) {
            throw new CommandException(String.format(MESSAGE_INVALID_TASK, personToEdit.getName()));
        }

        Task taskToEdit = tasks.get(targetTaskIndex.getZeroBased());
        Task editedTask = createEditedTask(taskToEdit, editTaskDescriptor);

        if (taskToEdit.equals(editedTask)) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        }
        tasks.set(targetTaskIndex.getZeroBased(), editedTask);

        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getTags(), tasks, personToEdit.getDescription(),
                personToEdit.isImportant()
        );
        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        CommandResult commandResult = new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, editedTask));
        commandResult.setWriteCommand();
        return commandResult;
    }

    private static Task createEditedTask(Task taskToEdit, EditTaskDescriptor editTaskDescriptor) {
        assert(taskToEdit != null);

        TaskName updatedName = editTaskDescriptor.getTaskName().orElse(taskToEdit.getTaskName());
        TaskDate updatedDate = editTaskDescriptor.getTaskDate().orElse(taskToEdit.getDate());
        TaskTime updatedTime = editTaskDescriptor.getTaskTime().orElse(taskToEdit.getTime());
        Venue updatedVenue = editTaskDescriptor.getTaskVenue().orElse(taskToEdit.getVenue());

        Task updatedTask = new Task(updatedName, updatedDate, updatedTime, updatedVenue);

        return updatedTask;
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditTaskCommand)) {
            return false;
        }

        // state check
        EditTaskCommand e = (EditTaskCommand) other;
        return targetPersonIndex.equals(e.targetPersonIndex)
                && targetTaskIndex.equals(e.targetTaskIndex)
                && editTaskDescriptor.equals(e.editTaskDescriptor);
    }

    public String getCommand() {
        return COMMAND_WORD;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Stores the details to edit the task with. Each non-empty field value will replace the
     * corresponding field value of the task.
     */
    public static class EditTaskDescriptor {
        private TaskName taskName;
        private TaskDate taskDate;
        private TaskTime taskTime;
        private Venue taskVenue;

        public EditTaskDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditTaskDescriptor(EditTaskDescriptor toCopy) {
            setTaskName(toCopy.taskName);
            setTaskDate(toCopy.taskDate);
            setTaskTime(toCopy.taskTime);
            setTaskVenue(toCopy.taskVenue);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(taskName, taskDate, taskTime, taskVenue);
        }

        public void setTaskName(TaskName taskName) {
            this.taskName = taskName;
        }

        public Optional<TaskName> getTaskName() {
            return Optional.ofNullable(taskName);
        }

        public void setTaskDate(TaskDate taskDate) {
            this.taskDate = taskDate;
        }

        public Optional<TaskDate> getTaskDate() {
            return Optional.ofNullable(taskDate);
        }

        public void setTaskTime(TaskTime taskTime) {
            this.taskTime = taskTime;
        }

        public Optional<TaskTime> getTaskTime() {
            return Optional.ofNullable(taskTime);
        }

        public void setTaskVenue(Venue venue) {
            this.taskVenue = taskVenue;
        }

        public Optional<Venue> getTaskVenue() {
            return Optional.ofNullable(taskVenue);
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditTaskDescriptor)) {
                return false;
            }

            // state check
            EditTaskDescriptor e = (EditTaskDescriptor) other;

            return getTaskName().equals(e.getTaskName())
                    && getTaskDate().equals(e.getTaskDate())
                    && getTaskTime().equals(e.getTaskTime())
                    && getTaskVenue().equals(e.getTaskVenue());
        }
    }
}
