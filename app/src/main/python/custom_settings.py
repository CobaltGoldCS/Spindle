from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry
from bs4 import BeautifulSoup
import lxml
import cloudscraper

# Decorators / Support Functions
def completeUrls(function: callable) -> list:
    """Completes incomplete urls

    Args:
        function (callable): The function decorated by this decorator

    Returns:
        list: content, title, prev_url, next_url
    """
    def wrapper(url: str, *args, **kwargs):
        baseUrl = "/".join(url.split("/")[:3])
        content, title, prev_url, next_url = function(url, *args, **kwargs)

        if prev_url is not None:
            prev_url = baseUrl + prev_url if prev_url.startswith("/") else prev_url
        if next_url is not None:
            next_url = baseUrl + next_url if next_url.startswith("/") else next_url

        return content, title, prev_url, next_url
    return wrapper

from traceback import print_exc
def errorHandler(function: callable):
    def wrapper(url: str, *args, **kwargs):
        try:
            return function(url, *args, **kwargs)
        except IndexError as e:
            print("Invalid Url")
            print_exc()
        except LookupError as e:
            print("Most likely a nonexistent or forbidden url")
            print_exc()
    return wrapper

# Use this session for getting data from URLs
# Its preset to bypass websites that ask for a valid user agent
SESSION = cloudscraper.CloudScraper()
retry   = Retry(connect=3, backoff_factor=0.5)
adapter = HTTPAdapter(max_retries=retry)
SESSION.mount('http://', adapter)
SESSION.mount('https://', adapter)
headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:79.0) Gecko/20100101 Firefox/79.0"}
SESSION.headers = headers



@completeUrls
def lnWorld(url : str) -> list:
    # Get text
    DATA = SESSION.get(url)
    # Error handling
    if   DATA.status_code == 404: raise LookupError("No Page", "No page accessible by that url; or website not allowing access")
    elif DATA.status_code == 403: raise LookupError("Forbidden", "Scraper not allowed access by ddos protection (probably)")

    SOUP = BeautifulSoup(DATA.text, 'html.parser')
    # Get Title
    try: title = SOUP.find("title").text
    except: title = "Error"

    storyTag = SOUP.find(class_= "chapter-content")
    print(SOUP.text)
    for match in storyTag.find_All("ul", recursive = False):
        match.unwrap()

    content = "\n\t\t".join([f"{element.getText()}" for element in
    storyTag.findAll(lambda tag: tag.name in ["p", "ul"])
    if element.get('class') is None]) # This line makes sure not to get any annoying ads
    #Buttons for next and previous chapters
    prevInfo = SOUP.find(lambda tag: tag.get('class') == ['chnav', 'prev'])
    if prevInfo != None:
        prev_url = prevInfo['href'] if prevInfo.get('href') != None and prevInfo['href'] != '#' else None
    else: prev_url = None

    nextInfo = SOUP.find(lambda tag: tag.get('class') == ['chnav', 'next'])
    if nextInfo != None:
        next_url = nextInfo['href'] if nextInfo.get('href') != None and nextInfo['href'] != '#' else None
    else: next_url = None
    return content, title, prev_url, next_url


import re
import json

@ errorHandler
@ completeUrls
def wattpad(url: str) -> list:

    ID = re.findall('/(\d{5,})', url)[0]

    apiCallUrl = f"https://www.wattpad.com/apiv2/info?id={ID}"
    DATA = SESSION.get(apiCallUrl)

    if   DATA.status_code == 404: raise LookupError ("No Page", "No page accessible by that url; or website not allowing access")
    elif DATA.status_code == 403: raise LookupError ("Forbidden", "Scraper not allowed access by ddos protection (probably)"    )

    bookJson = DATA.json()

    chaps = bookJson["group"]

    currentChap, prevTitle, nextTitle = None, None, None
    currentId = None
    # When next and previous are being run, they are ran in increments of 3 instead of one
    for chapterId in range(len(chaps)):
        currentChap = chaps[chapterId]
        if currentChap["ID"] == bookJson["id"]:
            # Get chapter ids from api
            if chapterId != 0:
                prevChap  = chaps[chapterId - 1]
                prevTitle = prevChap["TITLE"]
                prevId = prevChap["ID"]
            else: prevTitle = None
            if len(chaps) - 1 != chapterId:
                nextChap  = chaps[chapterId + 1]
                nextTitle = nextChap["TITLE"]
                nextId = nextChap["ID"]
            else: nextTitle = None

            currentId = currentChap["ID"]
            break
    if currentId is None:
        raise LookupError("Chapter Does not exist", "Chapter May have been deleted")

    base = re.findall("\d{5,}(.+)",bookJson["url"])[0] # <- Find the data after the id
    # Build a url using the actual title
    prev_url = f"https://www.wattpad.com/{prevId}{base}" if prevTitle != None else None
    next_url = f"https://www.wattpad.com/{nextId}{base}" if nextTitle != None else None

    content = SESSION.get(f"https://www.wattpad.com/apiv2/storytext?id={currentId}").text
    title = currentChap["TITLE"]

    SOUP = BeautifulSoup(content, "html.parser")
    content = "\n\t\t".join([f"{element.text}" for element in SOUP.findAll("p", whitespace = False)])

    return content, title, prev_url, next_url


@ errorHandler
def webnovel(url: str) -> list:
    """Gets data from webnovel.com chapters using undocumented API

    Args:
        url (str): The url of webnovel.com chapter

    Raises:
        LookupError: 404 Error; File not found
        LookupError: 403 Error; Forbidden

    Returns:
        list: content, title, prev_url, next_url
    """
    bookId, chapterId = re.findall('_(\d{5,})', url)
    apiCallUrl = f"https://www.webnovel.com/apiajax/chapter/GetContent?_csrfToken=94w9XBrUFO69c33tsjJ1rcElpIEJmkWqinj48dbH&bookId={bookId}&chapterId={chapterId}&_=1597089803712"
    DATA = SESSION.get(apiCallUrl)

    if DATA.status_code == 404:
        raise LookupError("No Page", "No page accessible by that url; or website not allowing access")
    elif DATA.status_code == 403:
        raise LookupError("Forbidden",     "Scraper not allowed access by ddos protection (probably)")

    # Get the data in json format
    SOUP = BeautifulSoup(DATA.text, 'lxml')
    body = SOUP.find('body')
    content = json.loads(body.text)

    title = content['data']['bookInfo']['bookName']
    bookTitle = title.lower().replace(" ", "-")

    chapterInfo = content['data']['chapterInfo']

    # IDs
    nextChapterId, prevChapterId = chapterInfo['nextChapterId'], chapterInfo['preChapterId' ]

    # Titles
    nextChapterTitle = chapterInfo['nextChapterName'].lower().replace(" ", "-") if nextChapterId != -1 else None
    prevChapterTitle = chapterInfo['preChapterName' ].lower().replace(" ", "-") if prevChapterId != -1 else None

    # Construct urls dynamically
    next_url = f"https://www.webnovel.com/book/{bookTitle}_{bookId}/{nextChapterTitle}_{nextChapterId}" if nextChapterTitle != None else None
    prev_url = f"https://www.webnovel.com/book/{bookTitle}_{bookId}/{prevChapterTitle}_{prevChapterId}" if prevChapterTitle != None else None

    content = [f"<p>{obj['content']}</p>" for obj in chapterInfo['contents']]
    content = "".join(content).replace("â¦", "...") # Reformatting data to be more readable and accessible

    SOUP = BeautifulSoup(content, "html.parser")
    content = "\n".join([f"{element.text}" for element in SOUP.findAll("p", whitespace = False)])

    return content, title, prev_url, next_url

@ errorHandler
@ completeUrls
def fanfnet(url: str) -> list:
    """Function to allow access to fanfiction.net content with a chapter url
    WARNING: LIKELY WILL NOT WORK DUE TO CLOUDFLARE ISSUES

    Args:
        url (str): The url of the target chapter of the story

    Raises:
        LookupError: 404 error; File not found

    Returns:
        list: a list containing 'content' of the story, 'title' of the story, 'prev_url' Url for previous chapter, 'next_url' Url for next chapter
    """
    title = url.split("/")[6].replace("-", " ").title()
    # Get text
    data = SESSION.get(url)
    if data.status_code == 404: raise LookupError("No Page", "No page accessible by that url")

    soup = BeautifulSoup(data.text, 'html.parser')
    div = soup.findChildren('div', id = "storytext")
    finalString = "".join([f"\n\n{str(element)}" for element in div[0].contents])
    content = BeautifulSoup(finalString.encode(), 'lxml').text

    #Buttons for next and previous chapters
    prev_url = soup.find('button', text = "< Prev")
    next_url = soup.find('button', text = "Next >")

    prev_url = prev_url['onclick'][14:]  if prev_url != None else None
    next_url = next_url["onclick"][14:]  if next_url != None else None

    return content, title, prev_url, next_url