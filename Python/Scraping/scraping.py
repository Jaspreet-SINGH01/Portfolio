import requests
from bs4 import BeautifulSoup

# Définissez l'URL du site Web que vous souhaitez scraper
url = "https://www.amazon.com/product/B07832J23H"

# Effectuez une demande GET sur l'URL
response = requests.get(url)

# Analysez la réponse en tant qu'objet BeautifulSoup
soup = BeautifulSoup(response.content, "html.parser")

# Trouvez l'élément de la balise `div` avec la classe `availability`
availability_element = soup.find("div", class_="availability")

# Obtenez la valeur de l'attribut `text` de l'élément
try:
    availability = availability_element.text
except:
    print("L'élément d'accessibilité n'a pas été trouvé.")

# Imprimez la valeur de la disponibilité
if availability is not None:
    print(f"La disponibilité du produit est {availability}.")
else:
    print("La disponibilité du produit n'a pas été trouvée.")
