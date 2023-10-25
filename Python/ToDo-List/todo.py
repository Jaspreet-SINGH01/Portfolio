from tkinter import *
from tkinter import ttk

# The use of Tkinter to create a GUI application.
# The use of a Listbox to display the tasks.
# The use of buttons to add and delete tasks.
# The use of comments to document the code.
# The use of consistent formatting.

class todo:
    def __init__(self, root):
        self.root = root
        self.root.title("To-do list")
        self.root.geometry("650x410+300+150")

        # Create a label with the text "To-do list App"
        self.label = Label(self.root, text="To-do list App",
                           font="ariel, 25 bold", width=10, bd=5, bg="orange", fg="black")
        self.label.pack(side="top", fill=BOTH)


        # Create a label with the text "Add task"
        self.label2 = Label(self.root, text="Add task",
                            font="ariel, 18 bold", width=10, bd=5, bg="orange", fg="black")
        self.label.place(x=40, y=54)


        # Create a label with the text "Tasks"
        self.label3 = Label(self.root, text="Tasks",
                            font="ariel, 18 bold", width=10, bd=5, bg="orange", fg="black")
        self.label.place(x=320, y=54)


        # Create a Listbox to display the tasks
        self.main_text = Listbox(
            self.root, height=9, bd=5, width=23, font="ariel, 20 italic bold")
        self.main_text.place(x=200, y=100)


        # Create a Text widget to enter the task
        self.text = Text(self.root, bd=5, height=2,
                         width=30, font="ariel, 10 bold")
        self.text.place(x=20, y=120)


        # Create a button to add the task
        self.button = Button(self.root, text="Add", font='sarif, 20 bold italic',
                                 width=10, bd=5, bg='orange', fg='black', command=add)
        self.button.place(x=30, y=100)
        

        # Create a button to delete the task
        self.button = Button(self.root, text="Delete", font='sarif, 20 bold italic',
                                 width=10, bd=5, bg='orange', fg='black', command=delete)
        self.button.place(x=30, y=200)

    def main():
        root = Tk()
        ui = todo(root)
        root.mainloop()

    if __name__ == "__main__":
        main()
