package net.orekyuu.gitthrow.ticket.port.table;

import org.seasar.doma.*;

import java.util.Objects;

@Entity(immutable = true)
@Table(name = "ticket_priority")
public class TicketPriorityTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private final Long id;
    @Column(name = "project")
    private final String project;
    @Column(name = "priority")
    private final String priority;

    public TicketPriorityTable(Long id, String project, String priority) {
        this.id = id;
        this.project = project;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public String getProject() {
        return project;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketPriorityTable that = (TicketPriorityTable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
