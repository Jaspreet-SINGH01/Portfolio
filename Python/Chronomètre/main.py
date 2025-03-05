import time

# Définissez les variables
start_time = None
end_time = None

# Définissez la fonction pour démarrer le chronomètre
def start_timer():
    global start_time
    start_time = time.time()

# Définissez la fonction pour arrêter le chronomètre
def stop_timer():
    global end_time
    end_time = time.time()

# Définissez la fonction pour afficher le temps écoulé
def show_elapsed_time():
    global start_time
    global end_time
    if start_time is None or end_time is None:
        print("Le chronomètre n'est pas en cours.")
    else:
        elapsed_time = end_time - start_time
        print("Le temps écoulé est de {} secondes".format(elapsed_time))

# Définissez la fonction pour afficher l'interface
def show_interface():
    print("Chronomètre")
    print("1. Démarrer")
    print("2. Arrêter")
    print("3. Afficher le temps écoulé")
    print("4. Quitter")

# Afficher l'interface
show_interface()

# Tant que l'utilisateur n'a pas choisi de quitter
while True:

    # Demander à l'utilisateur ce qu'il veut faire
    choice = input("Que voulez-vous faire ? ")

    # Traiter le choix de l'utilisateur
    if choice == "1":
        start_timer()
    elif choice == "2":
        stop_timer()
    elif choice == "3":
        show_elapsed_time()
    elif choice == "4":
        break

    # Afficher l'interface
    show_interface()
