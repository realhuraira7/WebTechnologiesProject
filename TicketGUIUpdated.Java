package ticketmanagementsystem; 
import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate; 

public class TicketGUI extends JFrame {

    static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; this.next = null; }
    }

    //Ticketconcept
    static class Ticket {
        private static int counter = 1000;
        private final int id;
        private final String customerName;
        private final String issue;
        private String status;

        Ticket(String customerName, String issue) {
            this.id = ++counter;
            this.customerName = customerName;
            this.issue = issue;
            this.status = "WAITING";
        }

        int getId() { return id; }
        void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return String.format("[#%d] %s - \"%s\" (%s)", id, customerName, issue, status);
        }
    }

    //LinkedList
    static class CustomLinkedList<T> {
        
         Node<T> head;
        private int size;

        void addLast(T data) {
            Node<T> newNode = new Node<>(data);
            if (head == null) {
                head = newNode;
            } else {
                Node<T> temp = head;
                while (temp.next != null) temp = temp.next;
                temp.next = newNode;
            }
            size++;
        }

        boolean isEmpty() { return size == 0; }

        //toremove
        //eitherduplicateddataofnode and return null if there is no matching data
        T remove(Predicate<T> matches) {
            Node<T> temp = head;
            Node<T> prev = null;
            while (temp != null) {
                if (matches.test(temp.data)) {
                    if (prev == null) {
                        head = temp.next;
                    } else {
                        prev.next = temp.next;
                    }
                    size--;
                    return temp.data;
                }
                prev = temp;
                temp = temp.next;
            }
            return null;
        }

        void display() {
            if (isEmpty()) {
                System.out.println("   (no tickets recorded yet)");
                return;
            }
            Node<T> temp = head;
            int count = 1;
            while (temp != null) {
                System.out.println("   " + count + ". " + temp.data);
                temp = temp.next;
                count++;
            }
        }
    }

    //Queue 
    static class CustomQueue<T> {
        private Node<T> front;
        private Node<T> rear;
        private int size;

        void enqueue(T data) {
            Node<T> newNode = new Node<>(data);
            if (rear == null) {
                front = newNode;
                rear = newNode;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
            size++;
        }

        T dequeue() {
            if (isEmpty()) throw new RuntimeException("Queue is empty - no one to serve!");
            T data = front.data;
            front = front.next;
            if (front == null) rear = null;
            size--;
            return data;
        }

        boolean isEmpty() { return size == 0; }

        //toremove
        //eitherduplicateddataofnode and return null if there is no matching data
        T remove(Predicate<T> matches) {
            Node<T> temp = front;
            Node<T> prev = null;
            while (temp != null) {
                if (matches.test(temp.data)) {
                    if (prev == null) {
                        front = temp.next;
                    } else {
                        prev.next = temp.next;
                    }
                    if (temp == rear) {
                        rear = prev;
                    }
                    size--;
                    return temp.data;
                }
                prev = temp;
                temp = temp.next;
            }
            return null;
        }

        void display() {
            if (isEmpty()) {
                System.out.println("   (queue is empty - no one waiting)");
                return;
            }
            Node<T> temp = front;
            System.out.print("   FRONT -> ");
            while (temp != null) {
                System.out.print(temp.data + (temp.next != null ? "  ->  " : ""));
                temp = temp.next;
            }
            System.out.println("  <- REAR");
        }
    }

    //Stack
    static class CustomStack<T> {
        private Node<T> top;
        private int size;

        void push(T data) {
            Node<T> newNode = new Node<>(data);
            newNode.next = top;
            top = newNode;
            size++;
        }

        T pop() {
            if (isEmpty()) throw new RuntimeException("Stack is empty - nothing to undo!");
            T data = top.data;
            top = top.next;
            size--;
            return data;
        }

        boolean isEmpty() { return size == 0; }
//toremove
//eitherduplicateddataofnode and return null if there is no matching data
        T remove(Predicate<T> matches) {
            Node<T> temp = top;
            Node<T> prev = null;
            while (temp != null) {
                if (matches.test(temp.data)) {
                    if (prev == null) {
                        top = temp.next;
                    } else {
                        prev.next = temp.next;
                    }
                    size--;
                    return temp.data;
                }
                prev = temp;
                temp = temp.next;
            }
            return null;
        }

        void display() {
            if (isEmpty()) {
                System.out.println("   (no history yet)");
                return;
            }
            Node<T> temp = top;
            System.out.println("   TOP");
            while (temp != null) {
                System.out.println("    | " + temp.data);
                temp = temp.next;
            }
            System.out.println("   BOTTOM");
        }
    }

    //GUI 
    private CustomLinkedList<Ticket> allTickets = new CustomLinkedList<>();
    private CustomQueue<Ticket> waitingQueue = new CustomQueue<>();
    private CustomStack<Ticket> servedHistory = new CustomStack<>();
    private int totalTickets = 0;
private int waitingTickets = 0;
private int servedTickets = 0;

    private JTextField nameField = new JTextField(10);
    private JTextField issueField = new JTextField(10);
    private JTextArea allArea = new JTextArea();
    private JTextArea queueArea = new JTextArea();
    private JTextArea stackArea = new JTextArea();
    private JLabel totalLabel = new JLabel("Total Tickets: 0");
private JLabel waitingLabel = new JLabel("Waiting: 0");
private JLabel servedLabel = new JLabel("Served: 0");

    public TicketGUI() {
        setTitle("Ticket Management System new vrsion");
        setSize(950, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(new JLabel("Issue:"));
        topPanel.add(issueField);

        JButton addBtn = new JButton("Add Ticket");
        JButton serveBtn = new JButton("Serve Next");
        JButton undoBtn = new JButton("Undo Last Serve");  
        JButton searchBtn = new JButton("Search Ticket"); 
        JButton deleteBtn = new JButton("Delete Ticket");
        topPanel.add(addBtn);
        topPanel.add(serveBtn);
        topPanel.add(undoBtn);
        topPanel.add(searchBtn);
        topPanel.add(deleteBtn);
        add(topPanel, BorderLayout.NORTH);
        JPanel statsPanel = new JPanel();

statsPanel.add(totalLabel);
statsPanel.add(waitingLabel);
statsPanel.add(servedLabel);

add(statsPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        centerPanel.add(makePanel("All Tickets (Linked List)", allArea));
        centerPanel.add(makePanel("Waiting Queue (FIFO)", queueArea));
        centerPanel.add(makePanel("Served History (Stack)", stackArea));
        add(centerPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addTicket());
        serveBtn.addActionListener(e -> serveNext());
        undoBtn.addActionListener(e -> undoServe());
        searchBtn.addActionListener(e -> searchTicket());
        deleteBtn.addActionListener(e -> deleteTicket());

        refreshDisplays();
    }

    private JPanel makePanel(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        area.setEditable(false);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void addTicket() {
        String name = nameField.getText().trim();
        String issue = issueField.getText().trim();
        if (name.isEmpty() || issue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both name and issue.");
            return;
        }
        Ticket ticket = new Ticket(name, issue);
        
        allTickets.addLast(ticket);
        waitingQueue.enqueue(ticket);
        totalTickets++;
waitingTickets++;
        nameField.setText("");
        issueField.setText("");
        refreshDisplays();
    }

    private void serveNext() {
        try {
            Ticket ticket = waitingQueue.dequeue();
            ticket.setStatus("SERVED");
            servedHistory.push(ticket);
            waitingTickets--;
servedTickets++;
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        refreshDisplays();
    }

    private void undoServe() {
        try {
            Ticket ticket = servedHistory.pop();
            ticket.setStatus("WAITING");
            waitingQueue.enqueue(ticket);
            waitingTickets++;
servedTickets--;
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        refreshDisplays();
    }
    private void searchTicket() {
        String input = JOptionPane.showInputDialog(this, "Enter Ticket ID:");

    if (input == null) {
        return;
    }

    int id;

    try {
        id = Integer.parseInt(input);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        return;
    }

    Node<Ticket> temp = allTickets.head;

    while (temp != null) {

        if (temp.data.getId() == id) {
            JOptionPane.showMessageDialog(this, temp.data.toString());
            return;
        }

        temp = temp.next;
    }

    JOptionPane.showMessageDialog(this, "Ticket not found.");

}

    private void deleteTicket() {
        String input = JOptionPane.showInputDialog(this, "Enter Ticket ID to delete:");

        if (input == null) {
            return;
        }

        int id;

        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            return;
        }
        Node<Ticket> temp = allTickets.head;
        Ticket found = null;
        while (temp != null) {
            if (temp.data.getId() == id) {
                found = temp.data;
                break;
            }
            temp = temp.next;
        }

        if (found == null) {
            JOptionPane.showMessageDialog(this, "Ticket not found.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete ticket " + found + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean wasWaiting = "WAITING".equals(found.status);
        boolean wasServed = "SERVED".equals(found.status);

        allTickets.remove(t -> t.getId() == id);

        if (wasWaiting) {
            waitingQueue.remove(t -> t.getId() == id);
            waitingTickets--;
        } else if (wasServed) {
            servedHistory.remove(t -> t.getId() == id);
            servedTickets--;
        }

        totalTickets--;
        refreshDisplays();
    }

    private void refreshDisplays() {
        allArea.setText(captureDisplay(allTickets::display));
        queueArea.setText(captureDisplay(waitingQueue::display));
        stackArea.setText(captureDisplay(servedHistory::display));
    
    totalLabel.setText("Total Tickets: " + totalTickets);
waitingLabel.setText("Waiting: " + waitingTickets);
servedLabel.setText("Served: " + servedTickets);}

    private String captureDisplay(Runnable displayCall) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream original = System.out;
        System.setOut(new java.io.PrintStream(baos));
        displayCall.run();
        System.setOut(original);
        return baos.toString();
    }
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new TicketGUI().setVisible(true));
}
}