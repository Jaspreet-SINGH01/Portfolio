from graphviz import Digraph

# Création de l'objet Digraph
uml = Digraph(comment='Pro\'Artist UML Diagram', format='png')

# Configuration générale
uml.attr('graph', splines='ortho', rankdir='TB')
uml.attr('node', shape='rectangle', style='rounded,filled', color='lightblue2', fontname='Arial', fontsize='12')
uml.attr('edge', fontname='Arial', fontsize='10')

# Définition des classes principales
uml.node('Platform', 'Pro\'Artist Platform\n+ User Management\n+ Sponsor Management\n+ Financial Management\n+ Activity Tracking\n+ Creation & Analysis Tools\n+ Marketplace & Services\n+ Community & Networking\n+ Rights Management\n+ Automation & Personalization\n+ Event & Webinars\n+ Gamification\n+ Messaging\n+ Social Media Tracking')

uml.node('User', 'User\n+ Name\n+ Email\n+ Role\n+ Preferences\n+ Language\n+ Portfolio')
uml.node('Sponsor', 'Sponsor\n+ Name\n+ Industry\n+ Contact Info\n+ Interests\n+ Campaigns')
uml.node('Admin', 'Admin\n+ Name\n+ Email\n+ Role\n+ Permissions')

# Sous-systèmes spécifiques
uml.node('UserMgmt', 'User Management\n+ Create Profile\n+ Manage Profiles\n+ Track User Activity')
uml.node('SponsorMgmt', 'Sponsor Management\n+ Search Sponsors\n+ Manage Relationships\n+ Track Collaborations')
uml.node('FinancialMgmt', 'Financial Management\n+ Create Invoices\n+ Analyze Invoices\n+ Payment Integration')
uml.node('ActivityTrack', 'Activity Tracking\n+ Dashboard\n+ Performance Tracking\n+ Automated Reports')
uml.node('CreationTools', 'Creation & Analysis Tools\n+ Content Creation\n+ Competitive Analysis\n+ Strategic Recommendations')
uml.node('Marketplace', 'Marketplace & Services\n+ Tool Access\n+ Art Sales\n+ Third-Party Services')
uml.node('Community', 'Community & Networking\n+ Forums\n+ Groups\n+ Mentorship')
uml.node('RightsMgmt', 'Rights Management\n+ Copyright Protection\n+ Licensing')
uml.node('Automation', 'Automation & Personalization\n+ Process Automation\n+ Personalized Dashboard\n+ Recommendation System')
uml.node('Events', 'Event & Webinars\n+ Event Management\n+ Webinars\n+ Replay & Resources')
uml.node('Gamification', 'Gamification\n+ Rewards\n+ Badges\n+ Engagement Tracking')
uml.node('Messaging', 'Messaging\n+ Direct Communication\n+ Notifications')
uml.node('SocialMedia', 'Social Media Tracking\n+ Reputation Monitoring\n+ Social Sharing\n+ Social Media Analysis')

# Définition des relations
uml.edge('Platform', 'UserMgmt')
uml.edge('Platform', 'SponsorMgmt')
uml.edge('Platform', 'FinancialMgmt')
uml.edge('Platform', 'ActivityTrack')
uml.edge('Platform', 'CreationTools')
uml.edge('Platform', 'Marketplace')
uml.edge('Platform', 'Community')
uml.edge('Platform', 'RightsMgmt')
uml.edge('Platform', 'Automation')
uml.edge('Platform', 'Events')
uml.edge('Platform', 'Gamification')
uml.edge('Platform', 'Messaging')
uml.edge('Platform', 'SocialMedia')

# Relations entre utilisateurs et système
uml.edge('User', 'Platform')
uml.edge('Sponsor', 'Platform')
uml.edge('Admin', 'Platform')

# Affichage de l'UML
uml.render('/mnt/data/ProArtist_UML')
