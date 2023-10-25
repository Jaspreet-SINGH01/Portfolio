import random

# Définissez la longueur du mot de passe
password_length = 12

# Définissez les caractères qui peuvent être utilisés dans le mot de passe
characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-={}[]|\;:'\",.<>/?"

# Générez le mot de passe
password = ""
for i in range(password_length):
    password += random.choice(characters)

# Imprimez le mot de passe
print(password)