# Chatbot simple

from tkinter import *
import datetime

root = Tk()

def envoie():
    envoie = "Moi : " +e.get()
    txt.insert(END, "\n" + envoie)
    if 'Bonjour' in e.get():
        txt.insert(END, "\n" + "Bot : Bonjour ! Comment allez-vous ? En quoi puis-je vous aider ? ")

    elif 'heure' in e.get():
        heure = datetime.datetime.now().strftime("%H:%M")
        txt.insert(END, "\n" + "Bot : il est" + heure)

    elif 'nom' in e.get():
        txt.insert(END, "\n" + "Bot : Je me nomme Bot")

    else :
        txt.insert(END, "\n" + "Bot : Désolé, je ne comprends pas votre demande")



    e.delete(0, END)



txt = Text(root, font=("Times New Roman", 15))
txt = Text(root)
txt.grid(row = 0, column = 0, columnspan=2)
e = Entry(root, width = 100)
e.grid(row = 1, column = 0)
envoyer = Button(root, text="Envoyer", command=envoie).grid(row=  1, column = 1)

root.title("Bot")
root.mainloop()