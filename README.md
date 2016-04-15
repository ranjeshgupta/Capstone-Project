# Capstone-Project
Udacity final project

#Stocks Portfolio Manager 

##Description 
Stocks Portfolio Manager for Investors on the move for tracking your stock portfolio and manage your quotes with social features.

##Portfolio Management & Tracking
- Manage and track multiple portfolios with beautiful snapshot view, transactions details, capital gain and quotes charts
- Enter your trades, dividends, splits and bonus
- 2 different Android Widgets (Summary and Details) at your command
- This app can be used anonymously without an account. 

##Intended User
This app targets the investors who have invested their money in Indian market (BSE or NSE indices)

##Features
Key features of this app:
- Manage and track multiple portfolios with beautiful snapshot view, transactions details and capital gain 
- Enter your trades, dividends, splits and bonus
- 2 different widgets (Summary widget and Collection widget)

##Data persistence
All portfolio details and investments and their transactions will be persistently stored using SQLite table database. Content provider will be used to show different views e.g. snapshot, transactions details and capital gain.

##Corner cases in the UX
The initial screen is Create a new portfolio. Then user has to set up his/her investments. Then user can view snapshot view, transactions details and capital gain.

##Snapshot: 
This view shows current balance unit and its market value of every stock of selected portfolio. Portfolio can be changed through setting menu if user has multiple portfolio.

##Transaction:
User can view all posted transactions e.g. purchase, sell, dividends, splits and bonuses.

##Capital Gain:
This view contains realised and unrealised gain for each investments.
