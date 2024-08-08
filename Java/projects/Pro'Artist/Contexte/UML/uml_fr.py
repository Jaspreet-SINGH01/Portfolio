# Création de l'objet Digraph en français
from graphviz import Digraph


uml_fr = Digraph(comment='Pro\'Artist UML Diagram', format='png')

# Configuration générale
uml_fr.attr('graph', splines='ortho', rankdir='TB')
uml_fr.attr('node', shape='rectangle', style='rounded,filled', color='lightblue2', fontname='Arial', fontsize='12')
uml_fr.attr('edge', fontname='Arial', fontsize='10')

# Définition des classes principales en français
uml_fr.node('Platform', 'Plateforme Pro\'Artist\n+ Gestion des utilisateurs\n+ Gestion des sponsors\n+ Gestion financière\n+ Suivi de l\'activité\n+ Outils de création & d\'analyse\n+ Marketplace & Services\n+ Communauté & Réseau\n+ Gestion des droits\n+ Automatisation & Personnalisation\n+ Événements & Webinaires\n+ Gamification\n+ Messagerie\n+ Suivi des réseaux sociaux')

uml_fr.node('User', 'Utilisateur\n+ Nom\n+ Email\n+ Rôle\n+ Préférences\n+ Langue\n+ Portfolio')
uml_fr.node('Sponsor', 'Sponsor\n+ Nom\n+ Industrie\n+ Infos de contact\n+ Intérêts\n+ Campagnes')
uml_fr.node('Admin', 'Administrateur\n+ Nom\n+ Email\n+ Rôle\n+ Permissions')

# Sous-systèmes spécifiques en français
uml_fr.node('UserMgmt', 'Gestion des utilisateurs\n+ Créer un profil\n+ Gérer les profils\n+ Suivre l\'activité des utilisateurs')
uml_fr.node('SponsorMgmt', 'Gestion des sponsors\n+ Rechercher des sponsors\n+ Gérer les relations\n+ Suivre les collaborations')
uml_fr.node('FinancialMgmt', 'Gestion financière\n+ Créer des factures\n+ Analyser des factures\n+ Intégration des paiements')
uml_fr.node('ActivityTrack', 'Suivi de l\'activité\n+ Tableau de bord\n+ Suivi des performances\n+ Rapports automatiques')
uml_fr.node('CreationTools', 'Outils de création & d\'analyse\n+ Création de contenu\n+ Veille concurrentielle\n+ Recommandations stratégiques')
uml_fr.node('Marketplace', 'Marketplace & Services\n+ Accès aux outils\n+ Vente d\'œuvres d\'art\n+ Services tiers')
uml_fr.node('Community', 'Communauté & Réseau\n+ Forums\n+ Groupes\n+ Mentorat')
uml_fr.node('RightsMgmt', 'Gestion des droits\n+ Protection des créations\n+ Gestion des licences')
uml_fr.node('Automation', 'Automatisation & Personnalisation\n+ Automatisation des processus\n+ Tableau de bord personnalisé\n+ Système de recommandations')
uml_fr.node('Events', 'Événements & Webinaires\n+ Gestion des événements\n+ Webinaires\n+ Replays & ressources')
uml_fr.node('Gamification', 'Gamification\n+ Récompenses\n+ Badges\n+ Suivi de l\'engagement')
uml_fr.node('Messaging', 'Messagerie\n+ Communication directe\n+ Notifications')
uml_fr.node('SocialMedia', 'Suivi des réseaux sociaux\n+ Surveillance de la réputation\n+ Partage sur les réseaux\n+ Analyse des réseaux sociaux')

# Définition des relations
uml_fr.edge('Platform', 'UserMgmt')
uml_fr.edge('Platform', 'SponsorMgmt')
uml_fr.edge('Platform', 'FinancialMgmt')
uml_fr.edge('Platform', 'ActivityTrack')
uml_fr.edge('Platform', 'CreationTools')
uml_fr.edge('Platform', 'Marketplace')
uml_fr.edge('Platform', 'Community')
uml_fr.edge('Platform', 'RightsMgmt')
uml_fr.edge('Platform', 'Automation')
uml_fr.edge('Platform', 'Events')
uml_fr.edge('Platform', 'Gamification')
uml_fr.edge('Platform', 'Messaging')
uml_fr.edge('Platform', 'SocialMedia')

# Relations entre utilisateurs et système
uml_fr.edge('User', 'Platform')
uml_fr.edge('Sponsor', 'Platform')
uml_fr.edge('Admin', 'Platform')

# Affichage de l'UML
uml_fr.render('/mnt/data/ProArtist_UML_FR')