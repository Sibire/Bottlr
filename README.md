<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a name="readme-top"></a>

<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->
<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->







<!-- PROJECT LOGO -->
<br />
<div align="center">
  <img src="https://raw.githubusercontent.com/Sibire/Bottlr/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp">
<h1 align="center">Bottlr</h1>
  <p align="center">
    A simple, lightweight tool for managing your liquor inventory.
    <br />
    <!-- TODO: ADD A DEMO VERSION -->
    <a href="https://github.com/Sibire/Bottlr"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/Sibire/Bottlr/blob/master/app/debug/Bottlr-Refactor-Build1.apk">View Demo</a>
    ·
    <a href="mailto:bottlrdev@gmail.com" target="Bottlr Bug Report">Report Bug</a>
    ·
    <a href="mailto:bottlrdev@gmail.com" target="Bottlr Feature Request">Request Feature</a>
  </p>
</div>

---

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

---

<!-- ABOUT THE PROJECT -->
<h3 align="center">About Bottlr</h3>


Bottlr is a simple, lightweight tool for managing your liquor inventory. Whether you're a personal collector, a bartender, or a manager, Bottlr has the cataloguing and inventory tools you'll need to manage your collection, regardless of size.

Features
Bottle Cataloguing: Easily add, edit, and manage your liquor bottles, with images and detailed information.
Social Media Sharing: Share your favorite bottles on social media platforms.
Cloud Storage: Remotely store your inventory data in the cloud with Firebase and Google SSO. No new registration required!
Google Shopping Integration: One-Click search functionality makes finding a listing for your favorite bottles a breeze.
Inventory Searching: Quickly find specific bottles or categories within your inventory.
<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<h3 align="center">Built With</h3>

- Java

- Android

- Firebase

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- GETTING STARTED -->
<h3 align="center">Getting Started</h3>

Prerequisites:

- Android 11.0 or Newer

---

<h3 align="center">Installation</h3>

Developers:

- Clone the repo https://github.com/Sibire/Bottlr.git

- Replace the googleservices.json file with your appropriate Firebase file.

Users:

- Download the app from https://github.com/Sibire/Bottlr/blob/master/app/release/Bottlr_0.17.apk

- Install it from your downloads folder.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- USAGE EXAMPLES -->
<h3 align="center">Usage</h3>

Once installed, you can start using Bottlr to manage your liquor inventory.

 Basic Functionalities:

  Add a Bottle: Press the "+" button from the home screen, and fill in relevant information.

  Edit a Bottle: Click the edit button from a bottle's detail view to edit its information.

  Search Inventory: Use the search bar to find specific bottles or filter by data field.

  Share on Social Media: Press the share icon to quickly export an editable share blurb to the platform of your choice.

  Shopping: Press the shopping icon to open a Google Shopping query for your selected bottle.

  Save Image: Press to download the selected bottle's image to your phone's gallery.

  Firebase Login: Navigate to Settings and log in using your existing Google account.

  Upload to Cloud: Adds any new bottles to your Firebase Cloud.

  Sync From Cloud: Downloads any new bottles from your firebase Cloud.

  Erase Cloud Storage: Erases all remote storage, does not affect local storage.

  ---

  A note on adding bottles:

  "Name" Is a required field, and will refer to the specific name of your bottle. This may also include the distillery, but usually excludes it.

  "Distillery" Refers to the specific manufacturer of the bottle.

  "Region" Specifies from where, this is usually found somewhere on the label.

  "Type" Is the type of spirit. Also usually found on the label, something such as Tequila, Scotch, Vodka, and so on.

  "Age" Refers to how long a spirit was aged, if any. Different spirits have different minimums, and some are not aged at all. Feel free to leave this empty if your spirit is not aged, or if it has no age statement.

  "ABV" Is short for Alcohol By Volume, and is listed as a percentage on the bottle. May also be shown as "Proof", where 1 proof = 0.5%

  "Rating" Is your subjective rating of the bottle, from 1-10.

  "Notes" Is for any notes you wish to add.

  "Keywords" Are to be separated by a comma, and are used for search optimization.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- ROADMAP -->
<h3 align="center">Roadmap</h3>

 NFC TAGGING: Quickly scan, edit, and save bottle data to commercially available NFC tags to simplify inventory management.
 
 LOCATION TRACKING: Find a bottle you like? Whether it's on a store shelf or a bar cabinet, save your destination for future visits.

 COCKTAIL BOOK: Add your own recipes, and keep track of what you can make with your current inventory.
 
 PERSONALIZED SUGGESIONS: Find new cocktails and spirits based on your existing likes and inventory.

 DATABASE LOOKUP: Tired of adding data manually? Quickly search for and add a bottle from an array of pre-populated selections.

 COST-PER-POUR CALCULATION: Quickly and eaisly determine ideal prices for your cocktails and pours, based on volume and material cost.

 CONSUMPTION TRACKER: Keep tabs on your and your patrons' service, and avoid any unpleasant hangovers from overservice.
 
See the open issues for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>
<!-- CONTRIBUTING -->

---

<h3 align="center">Contributing</h3>

This is a closed team not accepting any external contributions.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- LICENSE -->
<h3 align="center">License</h3>

Distributed under the MIT License. See LICENSE.txt for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- CONTACT -->
<h3 align="center">Contact</h3>

Aoife Komperda (Sibire) & Michael George (Mike-Legend)

bottlrdev@gmail.com

Project Link: https://github.com/sibire/bottlr

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- ACKNOWLEDGMENTS -->
<h3 align="center">Acknowledgments</h3>

Bottlr is currently in Alpha status, version 0.12.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
