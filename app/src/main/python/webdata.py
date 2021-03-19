from bs4 import BeautifulSoup
import lxml
from custom_settings import *
# The one public method

class UrlReading:
    def __init__(self, url: str):
        """
        Info
        ----------------
        This object stores all basic needs for a reader, such as the next page, previous page, etc.
        
        Parameters
        ----------------
        url: A string containing a url for the application to scrape from

        Class attributes
        ----------------
        website: defined when domain matches with a url,
        a lambda with the value webpage which will get a matching configuration
        for getting data from that webpage \n
        
        current: The Url of the current content displayed on the page \n
        content: The chapter content of whatever page i'm looking at \n
        title: The title of the page \n
        prev, next: The urls that allow going to the previous and next chapters
        """
        domain = url.split("/")[2].replace("www.", "")
        websites = {
        "readnovelfull.com"   : (lambda webpage: generalWebsite(webpage, "chr-c",
                                lambda tag: tag.get('id') == "prev_chap",
                                lambda tag: tag.get('id') == "next_chap")),
        "royalroad.com"       : (lambda webpage: generalWebsite(webpage, "chapter-content",
                                lambda tag: "Previous Chapter" in tag.text and tag.name == 'a',
                                lambda tag: "Next Chapter"     in tag.text and tag.name == 'a')),
        "scribblehub.com"     : (lambda webpage: generalWebsite(webpage, 'chp_raw',
                                lambda tag: "Previous" == tag.text,
                                lambda tag: "Next"     == tag.text)),
        "boxnovel.net"        : (lambda webpage: generalWebsite(webpage, 'text-content',
                                lambda tag: tag.get('class') == ['btn', 'prev_page'],
                                lambda tag: tag.get('class') == ['btn', 'next_page'])),
        "novelhall.com"       : (lambda webpage: generalWebsite(webpage, "entry-content",
                                lambda tag: "Previous" in tag.text and tag.name == 'a',
                                lambda tag: "Next" in tag.text and tag.name ==     'a', forceAllText = True)),
        "readlightnovel.org"  : (lambda webpage: generalWebsite(webpage, "desc",
                                lambda tag: tag.get('class') == ["prev", "prev-link"],
                                lambda tag: tag.get('class') == ["next", "next-link"],
                                storyParams = {"name": "p", "recursive" : False, "text" : True})),
        "lightnovelworld.com" : (lambda webpage: lnWorld  (webpage)),
        "fanfiction.net"      : (lambda webpage: fanfnet  (webpage)),
        "wattpad.com"         : (lambda webpage: wattpad  (webpage)),
        "webnovel.com"        : (lambda webpage: webnovel (webpage))}
        try:
            self.website = websites[domain]
        except KeyError:
            self.website = testForCommonTemplates(url)
        self.current = url


    @ property
    def current(self):
        return self._current

    @ current.setter
    def current(self, value: str):
        """Setter method for current; Also will set the other values"""
        self.content, self.title, self.prev, self.next = self.website(value)
        self._current = value

#
#             PRIVATE
#             METHODS
#
#

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



def testForCommonTemplates(url : str):
    # Looks for simple templated code using BeautifulSoup
    soup = BeautifulSoup(SESSION.get(url).text, 'lxml')
    if soup.find(class_ = "text-left") != None:
        return lambda website: madara(website, soup)
    elif soup.find(class_ = "entry-content"): 
        # Common (default) tl website template
        return lambda website: generalWebsite(website, 'entry-content',
        lambda tag: tag.name == 'a' and "prev" in tag.text.lower(), 
        lambda tag: tag.name == 'a' and "next" in tag.text.lower(), soup = soup)
    raise LookupError("No Templates", "No templates or builtin lookup support available for this url")

# Template for Madara
def madara(url : str, soup) -> list:
    return generalWebsite(url,'text-left',
    lambda tag: tag.get('class') == ['btn', 'prev_page'],
    lambda tag: tag.get('class') == ['btn', 'next_page'], soup = soup)


@ errorHandler
@ completeUrls
def generalWebsite(URL: str, storyClass: str, prevLambda: bool, nextLambda: bool, **kwargs) -> list:
    """General Purpose webscraper for these types of websites

    Args:
        url (str): The url of the target website
        storyClass (str): The class that contains the text or the div with text inside of it
        prevLambda (callable): Lambda for filtering for a tag
        nextLambda (callable): Same as prevLambda, except target a tag should be the next page
    Optional Kwargs:   
        soup (BeautifulSoup): Running beautifulSoup instance, if applicable
        text (Bool): Get text only
        forceAllText (Bool): Forces storytag to get text, even if it is a div
        storyLambda (Lambda): Selector for things in the storyclass
        storyParams (dict): Dictionary for keyword args in soup.find

    Raises:
        LookupError: 404 Error; File not found
        LookupError: 403 Error; Forbidden url

    Returns:
        list: Contains [content, title, url for previous page, url for next page] in that order
    """
    if soup := kwargs.get("soup"):  SOUP = soup
    else:
        # Get text
        DATA = SESSION.get(URL)
        # Error handling
        if   DATA.status_code == 404: raise LookupError("No Page", "No page accessible by that url; or website not allowing access")
        elif DATA.status_code == 403: raise LookupError("Forbidden", "Scraper not allowed access by ddos protection (probably)"    )

        SOUP = BeautifulSoup(DATA.text, 'lxml')

    # Get Title
    try: title = SOUP.title.text
    except: title = "Error"
    # Checks if there are multiple separate p tags, which seems to be pretty common
    # for these types of websites
    storyTag = SOUP.find(class_ = storyClass)
    if storyLambda := kwargs.get("storyLambda"):
        content = "\n\t\t".join([f"{element.text}" for element in storyTag.findAll(storyLambda)])

    if not storyTag.name == 'div' or kwargs.get("forceAllText"): content = storyTag.text
    else:
        if kwargs.get("text") == True:
            content = "\n\t\t".join([f"{element.text}" for element in storyTag.findAll(text = True)])
        elif storyParameters := kwargs.get("storyParams"):
            content = "\n\t\t".join([f"{element.text}" for element in storyTag.findAll(**storyParameters)])
        else:
            content = "\n\t\t".join([f"{element.text}" for element in storyTag.findAll("p", whitespace = False)])
    #Buttons for next and previous chapters
    prevInfo = SOUP.find(prevLambda)
    if prevInfo != None:
        prev_url = prevInfo['href'] if prevInfo.get('href') != None and prevInfo['href'] != '#' else None
    else: prev_url = None

    nextInfo = SOUP.find(nextLambda)
    if nextInfo != None:
        next_url = nextInfo['href'] if nextInfo.get('href') != None and nextInfo['href'] != '#' else None
    else: next_url = None
    return content, title, prev_url, next_url

print(UrlReading("https://www.readlightnovel.org/i-was-caught-up-in-a-hero-summoning-but-that-world-is-at-peace/chapter-7").content)