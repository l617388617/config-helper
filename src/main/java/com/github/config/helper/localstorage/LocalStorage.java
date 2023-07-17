package com.github.config.helper.localstorage;

import com.github.config.helper.Settings;
import com.github.config.helper.component.CommonComponent;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * LocalStorage
 *
 * @author lupeng10
 * @create 2023-05-22 17:23
 */
public class LocalStorage {

    private static final Logger log = Logger.getInstance(LocalStorage.class);

    private static final Set<Project> projects = new HashSet<>();

    public static void addProject(Project project) {
        projects.add(project);
    }

    public static Project getProject() {
        return projects.stream().filter(Project::isOpen).findFirst().orElse(null);
    }

    private static final Set<String> names = new HashSet<>();

    static {
        names.add("ec");
        names.add("_bu");
        names.add("58tj_uuid");
        names.add("wmda_uuid");
        names.add("wmda_new_uuid");
        names.add("wmda_visited_projects");
        names.add("new_uv");
        names.add("_exid");
        names.add("_efmdata");
        names.add("dunCookie");
        names.add("csc");
        names.add("ishare_sso_username");
        names.add("sso_ticket");
    }

    private static String getPythonCommand() {
        //noinspection StringBufferReplaceableByString
        StringBuilder builder = new StringBuilder(1024 * 1024 * 18);
        builder.append("# -*- coding: utf-8 -*-\n");
        builder.append("import os\n");
        builder.append("import sys\n");
        builder.append("import time\n");
        builder.append("import glob\n");
        builder.append("try:\n");
        builder.append("    import cookielib\n");
        builder.append("except ImportError:\n");
        builder.append("    import http.cookiejar as cookielib\n");
        builder.append("from contextlib import contextmanager\n");
        builder.append("import tempfile\n");
        builder.append("try:\n");
        builder.append("    import json\n");
        builder.append("except ImportError:\n");
        builder.append("    import simplejson as json\n");
        builder.append("try:\n");
        builder.append("    import ConfigParser as configparser\n");
        builder.append("except ImportError:\n");
        builder.append("    import configparser\n");
        builder.append("try:\n");
        builder.append("    # should use Cryptodome in windows instead of Crypto\n");
        builder.append("    # otherwise will raise an import error\n");
        builder.append("    from Cryptodome.Protocol.KDF import PBKDF2\n");
        builder.append("    from Cryptodome.Cipher import AES\n");
        builder.append("except ImportError:\n");
        builder.append("    from Crypto.Protocol.KDF import PBKDF2\n");
        builder.append("    from Crypto.Cipher import AES\n");
        builder.append("\n");
        builder.append("try:\n");
        builder.append("    from pysqlite2 import dbapi2 as sqlite3\n");
        builder.append("except ImportError:\n");
        builder.append("    import sqlite3\n");
        builder.append("\n");
        builder.append("if sys.platform == 'darwin': # darwin is OSX\n");
        builder.append("    from struct import unpack\n");
        builder.append("    try:\n");
        builder.append("        from StringIO import StringIO # only works for python2\n");
        builder.append("    except ImportError:\n");
        builder.append("        from io import BytesIO as StringIO # only works for python3\n");
        builder.append("\n");
        builder.append("import lz4.block\n");
        builder.append("import keyring\n");
        builder.append("\n");
        builder.append("class BrowserCookieError(Exception):\n");
        builder.append("    pass\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("@contextmanager\n");
        builder.append("def create_local_copy(cookie_file):\n");
        builder.append("    # check if cookie file exists\n");
        builder.append("    if os.path.exists(cookie_file):\n");
        builder.append("        # copy to random name in tmp folder\n");
        builder.append("        tmp_cookie_file = tempfile.NamedTemporaryFile(suffix='.sqlite').name\n");
        builder.append("        open(tmp_cookie_file, 'wb').write(open(cookie_file, 'rb').read())\n");
        builder.append("        yield tmp_cookie_file\n");
        builder.append("    else:\n");
        builder.append("        raise BrowserCookieError('Can not find cookie file at: ' + cookie_file)\n");
        builder.append("\n");
        builder.append("    os.remove(tmp_cookie_file)\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("class BrowserCookieLoader(object):\n");
        builder.append("    def __init__(self, cookie_files=None):\n");
        builder.append("        cookie_files = cookie_files or self.find_cookie_files()\n");
        builder.append("        self.cookie_files = list(cookie_files)\n");
        builder.append("\n");
        builder.append("    def find_cookie_files(self):\n");
        builder.append("        '''Return a list of cookie file locations valid for this loader'''\n");
        builder.append("        raise NotImplementedError\n");
        builder.append("\n");
        builder.append("    def get_cookies(self):\n");
        builder.append("        '''Return all cookies (May include duplicates from different sources)'''\n");
        builder.append("        raise NotImplementedError\n");
        builder.append("\n");
        builder.append("    def load(self):\n");
        builder.append("        '''Load cookies into a cookiejar'''\n");
        builder.append("        cookie_jar = cookielib.CookieJar()\n");
        builder.append("        for cookie in self.get_cookies():\n");
        builder.append("            cookie_jar.set_cookie(cookie)\n");
        builder.append("        return cookie_jar\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("class Chrome(BrowserCookieLoader):\n");
        builder.append("    def __str__(self):\n");
        builder.append("        return 'chrome'\n");
        builder.append("\n");
        builder.append("    def find_cookie_files(self):\n");
        builder.append("        for pattern in [\n");
        builder.append("            os.path.expanduser('~/Library/Application Support/Google/Chrome/Default/Cookies'),\n");
        builder.append("            os.path.expanduser('~/Library/Application Support/Google/Chrome/Profile */Cookies'),\n");
        builder.append("            os.path.expanduser('~/Library/Application Support/Vivaldi/Default/Cookies'),\n");
        builder.append("            os.path.expanduser('~/Library/Application Support/Vivaldi/Profile */Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/chromium/Default/Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/chromium/Profile */Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/google-chrome/Default/Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/google-chrome/Profile */Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/vivaldi/Default/Cookies'),\n");
        builder.append("            os.path.expanduser('~/.config/vivaldi/Profile */Cookies'),\n");
        builder.append("            os.path.join(os.getenv('APPDATA', ''), r'..\\Local\\Google\\Chrome\\User Data\\Default\\Cookies'),\n");
        builder.append("            os.path.join(os.getenv('APPDATA', ''), r'..\\Local\\Google\\Chrome\\User Data\\Profile *\\Cookies'),\n");
        builder.append("            os.path.join(os.getenv('APPDATA', ''), r'..\\Local\\Vivaldi\\User Data\\Default\\Cookies'),\n");
        builder.append("            os.path.join(os.getenv('APPDATA', ''), r'..\\Local\\Vivaldi\\User Data\\Profile *\\Cookies'),\n");
        builder.append("        ]:\n");
        builder.append("            for result in glob.glob(pattern):\n");
        builder.append("                yield result\n");
        builder.append("\n");
        builder.append("    def get_cookies(self):\n");
        builder.append("        salt = b'saltysalt'\n");
        builder.append("        length = 16\n");
        builder.append("        if sys.platform == 'darwin':\n");
        builder.append("            # running Chrome on OSX\n");
        builder.append("            my_pass = keyring.get_password('Chrome Safe Storage', 'Chrome')\n");
        builder.append("            my_pass = my_pass.encode('utf8')\n");
        builder.append("            iterations = 1003\n");
        builder.append("            key = PBKDF2(my_pass, salt, length, iterations)\n");
        builder.append("\n");
        builder.append("        elif sys.platform.startswith('linux'):\n");
        builder.append("            # running Chrome on Linux\n");
        builder.append("            my_pass = 'peanuts'.encode('utf8')\n");
        builder.append("            iterations = 1\n");
        builder.append("            key = PBKDF2(my_pass, salt, length, iterations)\n");
        builder.append("\n");
        builder.append("        elif sys.platform == 'win32':\n");
        builder.append("            key = None\n");
        builder.append("        else:\n");
        builder.append("            raise BrowserCookieError('Unsupported operating system: ' + sys.platform)\n");
        builder.append("\n");
        builder.append("        for cookie_file in self.cookie_files:\n");
        builder.append("            with create_local_copy(cookie_file) as tmp_cookie_file:\n");
        builder.append("                con = sqlite3.connect(tmp_cookie_file)\n");
        builder.append("                cur = con.cursor()\n");
        builder.append("                cur.execute('SELECT value FROM meta WHERE key = \"version\";')\n");
        builder.append("                version = int(cur.fetchone()[0])\n");
        builder.append("                query = 'SELECT host_key, path, is_secure, expires_utc, name, value, encrypted_value FROM cookies;'\n");
        builder.append("                if version < 10:\n");
        builder.append("                    query = query.replace('is_', '')\n");
        builder.append("                cur.execute(query)\n");
        builder.append("                for item in cur.fetchall():\n");
        builder.append("                    host, path, secure, expires, name = item[:5]\n");
        builder.append("                    value = self._decrypt(item[5], item[6], key=key)\n");
        builder.append("                    yield create_cookie(host, path, secure, expires, name, value)\n");
        builder.append("                con.close()\n");
        builder.append("\n");
        builder.append("    def _decrypt(self, value, encrypted_value, key):\n");
        builder.append("        if (sys.platform == 'darwin') or sys.platform.startswith('linux'):\n");
        builder.append("            if value or (encrypted_value[:3] != b'v10'):\n");
        builder.append("                return value\n");
        builder.append("\n");
        builder.append("            # Encrypted cookies should be prefixed with 'v10' according to the\n");
        builder.append("            # Chromium code. Strip it off.\n");
        builder.append("            encrypted_value = encrypted_value[3:]\n");
        builder.append("\n");
        builder.append("            def clean(x):\n");
        builder.append("                last = x[-1]\n");
        builder.append("                if isinstance(last, int):\n");
        builder.append("                    return x[:-last].decode('utf8')\n");
        builder.append("                else:\n");
        builder.append("                    return x[:-ord(last)].decode('utf8')\n");
        builder.append("\n");
        builder.append("            iv = b' ' * 16\n");
        builder.append("            cipher = AES.new(key, AES.MODE_CBC, IV=iv)\n");
        builder.append("            decrypted = cipher.decrypt(encrypted_value)\n");
        builder.append("            return clean(decrypted)\n");
        builder.append("        else:\n");
        builder.append("            # Must be win32 (on win32, all chrome cookies are encrypted)\n");
        builder.append("            try:\n");
        builder.append("                import win32crypt\n");
        builder.append("            except ImportError:\n");
        builder.append("                raise BrowserCookieError('win32crypt must be available to decrypt Chrome cookie on Windows')\n");
        builder.append("            return win32crypt.CryptUnprotectData(encrypted_value, None, None, None, 0)[1].decode('utf-8')\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("class Firefox(BrowserCookieLoader):\n");
        builder.append("    def __str__(self):\n");
        builder.append("        return 'firefox'\n");
        builder.append("\n");
        builder.append("    def parse_profile(self, profile):\n");
        builder.append("        cp = configparser.ConfigParser()\n");
        builder.append("        cp.read(profile)\n");
        builder.append("        path = None\n");
        builder.append("        for section in cp.sections():\n");
        builder.append("            try:\n");
        builder.append("                if cp.getboolean(section, 'IsRelative'):\n");
        builder.append("                    path = os.path.dirname(profile) + '/' + cp.get(section, 'Path')\n");
        builder.append("                else:\n");
        builder.append("                    path = cp.get(section, 'Path')\n");
        builder.append("                if cp.has_option(section, 'Default'):\n");
        builder.append("                    return os.path.abspath(os.path.expanduser(path))\n");
        builder.append("            except configparser.NoOptionError:\n");
        builder.append("                pass\n");
        builder.append("        if path:\n");
        builder.append("            return os.path.abspath(os.path.expanduser(path))\n");
        builder.append("        raise BrowserCookieError('No default Firefox profile found')\n");
        builder.append("\n");
        builder.append("    def find_default_profile(self):\n");
        builder.append("        if sys.platform == 'darwin':\n");
        builder.append("            return glob.glob(os.path.expanduser('~/Library/Application Support/Firefox/profiles.ini'))\n");
        builder.append("        elif sys.platform.startswith('linux'):\n");
        builder.append("            return glob.glob(os.path.expanduser('~/.mozilla/firefox/profiles.ini'))\n");
        builder.append("        elif sys.platform == 'win32':\n");
        builder.append("            return glob.glob(os.path.join(os.getenv('APPDATA', ''), 'Mozilla/Firefox/profiles.ini'))\n");
        builder.append("        else:\n");
        builder.append("            raise BrowserCookieError('Unsupported operating system: ' + sys.platform)\n");
        builder.append("\n");
        builder.append("    def find_cookie_files(self):\n");
        builder.append("        profile = self.find_default_profile()\n");
        builder.append("        if not profile:\n");
        builder.append("            raise BrowserCookieError('Could not find default Firefox profile')\n");
        builder.append("        path = self.parse_profile(profile[0])\n");
        builder.append("        if not path:\n");
        builder.append("            raise BrowserCookieError('Could not find path to default Firefox profile')\n");
        builder.append("        cookie_files = glob.glob(os.path.expanduser(path + '/cookies.sqlite'))\n");
        builder.append("        if cookie_files:\n");
        builder.append("            return cookie_files\n");
        builder.append("        else:\n");
        builder.append("            raise BrowserCookieError('Failed to find Firefox cookies')\n");
        builder.append("\n");
        builder.append("    def get_cookies(self):\n");
        builder.append("        for cookie_file in self.cookie_files:\n");
        builder.append("            with create_local_copy(cookie_file) as tmp_cookie_file:\n");
        builder.append("                con = sqlite3.connect(tmp_cookie_file)\n");
        builder.append("                cur = con.cursor()\n");
        builder.append("                cur.execute('select host, path, isSecure, expiry, name, value from moz_cookies')\n");
        builder.append("\n");
        builder.append("                for item in cur.fetchall():\n");
        builder.append("                    yield create_cookie(*item)\n");
        builder.append("                con.close()\n");
        builder.append("\n");
        builder.append("                # current sessions are saved in sessionstore.js/recovery.json/recovery.jsonlz4\n");
        builder.append("                session_files = (os.path.join(os.path.dirname(cookie_file), 'sessionstore.js'),\n");
        builder.append("                    os.path.join(os.path.dirname(cookie_file), 'sessionstore-backups', 'recovery.js'),\n");
        builder.append("                    os.path.join(os.path.dirname(cookie_file), 'sessionstore-backups', 'recovery.json'),\n");
        builder.append("                    os.path.join(os.path.dirname(cookie_file), 'sessionstore-backups', 'recovery.jsonlz4'))\n");
        builder.append("                for file_path in session_files:\n");
        builder.append("                    if os.path.exists(file_path):\n");
        builder.append("                        if file_path.endswith('4'):\n");
        builder.append("                            try:\n");
        builder.append("                                session_file = open(file_path, 'rb')\n");
        builder.append("                                # skip the first 8 bytes to avoid decompress failure (custom Mozilla header)\n");
        builder.append("                                session_file.seek(8)\n");
        builder.append("                                json_data = json.loads(lz4.block.decompress(session_file.read()).decode())\n");
        builder.append("                            except IOError as e:\n");
        builder.append("                                print('Could not read file:', str(e))\n");
        builder.append("                            except ValueError as e:\n");
        builder.append("                                print('Error parsing Firefox session file:', str(e))\n");
        builder.append("                        else:\n");
        builder.append("                            try:\n");
        builder.append("                                json_data = json.loads(open(file_path, 'rb').read().decode('utf-8'))\n");
        builder.append("                            except IOError as e:\n");
        builder.append("                                print('Could not read file:', str(e))\n");
        builder.append("                            except ValueError as e:\n");
        builder.append("                                print('Error parsing firefox session JSON:', str(e))\n");
        builder.append("\n");
        builder.append("                if 'json_data' in locals():\n");
        builder.append("                    expires = str(int(time.time()) + 3600 * 24 * 7)\n");
        builder.append("                    for window in json_data.get('windows', []):\n");
        builder.append("                        for cookie in window.get('cookies', []):\n");
        builder.append("                            yield create_cookie(cookie.get('host', ''), cookie.get('path', ''), False, expires, cookie.get('name', ''), cookie.get('value', ''))\n");
        builder.append("                else:\n");
        builder.append("                    print('Could not find any Firefox session files')\n");
        builder.append("\n");
        builder.append("class Safari(BrowserCookieLoader):\n");
        builder.append("    def __str__(self):\n");
        builder.append("        return 'safari'\n");
        builder.append("\n");
        builder.append("    def find_cookie_files(self):\n");
        builder.append("        if (sys.platform != 'darwin'):  # checks if using OSX\n");
        builder.append("            BrowserCookieError('Safari is only available on OSX')\n");
        builder.append("        else:\n");
        builder.append("            cookie_files = glob.glob(os.path.expanduser('~/Library/Cookies'))\n");
        builder.append("            if cookie_files:\n");
        builder.append("                return cookie_files\n");
        builder.append("            else:\n");
        builder.append("                raise BrowserCookieError('Failed to find Safari cookies')\n");
        builder.append("\n");
        builder.append("    def get_cookies(self):\n");
        builder.append("        FilePath = os.path.expanduser('~/Library/Cookies/Cookies.binarycookies')\n");
        builder.append("\n");
        builder.append("        try:\n");
        builder.append("            binary_file = open(FilePath, 'rb')\n");
        builder.append("        except IOError:\n");
        builder.append("            BrowserCookieError('File Not Found :' + FilePath)\n");
        builder.append("            exit()\n");
        builder.append("\n");
        builder.append("        binary_file.read(4)# will equal 'cook', which stands for cookies\n");
        builder.append("\n");
        builder.append("        num_pages = unpack('>i', binary_file.read(4))[0]\n");
        builder.append("\n");
        builder.append("        page_sizes = []\n");
        builder.append("        for _ in range(num_pages):\n");
        builder.append("            page_sizes.append(unpack('>i', binary_file.read(4))[0])\n");
        builder.append("\n");
        builder.append("        pages = []\n");
        builder.append("        for ps in page_sizes:\n");
        builder.append("            pages.append(binary_file.read(ps))\n");
        builder.append("\n");
        builder.append("        for page in pages:\n");
        builder.append("            page = StringIO(page)\n");
        builder.append("            page.read(4)\n");
        builder.append("            num_cookies = unpack('<i', page.read(4))[0]\n");
        builder.append("\n");
        builder.append("            cookie_offsets = []\n");
        builder.append("            for _ in range(num_cookies):\n");
        builder.append("                cookie_offsets.append(unpack('<i', page.read(4))[0])\n");
        builder.append("\n");
        builder.append("            page.read(4)\n");
        builder.append("\n");
        builder.append("            cookie = ''\n");
        builder.append("            for offset in cookie_offsets:\n");
        builder.append("                page.seek(offset)\n");
        builder.append("                cookiesize = unpack('<i', page.read(4))[0]\n");
        builder.append("                cookie = StringIO(page.read(cookiesize))\n");
        builder.append("\n");
        builder.append("                cookie.read(4)\n");
        builder.append("\n");
        builder.append("                flags = unpack('<i', cookie.read(4))[0]\n");
        builder.append("                cookie_flags = ''\n");
        builder.append("                if flags == 0:\n");
        builder.append("                    cookie_flags = False # if nothing at all\n");
        builder.append("                if flags == 1:\n");
        builder.append("                    cookie_flags = True # if Secure\n");
        builder.append("                elif flags == 4:\n");
        builder.append("                    cookie_flags = False # if Http only\n");
        builder.append("                elif flags == 5:\n");
        builder.append("                    cookie_flags = True # if Secure and Http only\n");
        builder.append("                else:\n");
        builder.append("                    cookie_flags = False # if Unknown\n");
        builder.append("\n");
        builder.append("                cookie.read(4)\n");
        builder.append("\n");
        builder.append("                urloffset = unpack('<i', cookie.read(4))[0]\n");
        builder.append("                nameoffset = unpack('<i', cookie.read(4))[0]\n");
        builder.append("                pathoffset = unpack('<i', cookie.read(4))[0]\n");
        builder.append("                valueoffset = unpack('<i', cookie.read(4))[0]\n");
        builder.append("\n");
        builder.append("                expiry_date = str(int(unpack('<d', cookie.read(8))[0] + 978307200))\n");
        builder.append("\n");
        builder.append("                # create_date = str(int(unpack('<d', cookie.read(8))[0] + 978307200)) no need of it here...\n");
        builder.append("\n");
        builder.append("                # endofcookie = cookie.read(8) no need it either...\n");
        builder.append("\n");
        builder.append("                cookie.seek(urloffset - 4)\n");
        builder.append("                host = ''\n");
        builder.append("                u = cookie.read(1)\n");
        builder.append("                while unpack('<b', u)[0] != 0:\n");
        builder.append("                    host = host + u.decode('utf-8') # in bytes have to be decoded\n");
        builder.append("                    u = cookie.read(1)\n");
        builder.append("\n");
        builder.append("                cookie.seek(nameoffset - 4)\n");
        builder.append("                name = ''\n");
        builder.append("                n = cookie.read(1)\n");
        builder.append("                while unpack('<b', n)[0] != 0:\n");
        builder.append("                    name = name + n.decode('utf-8')\n");
        builder.append("                    n = cookie.read(1)\n");
        builder.append("\n");
        builder.append("                cookie.seek(pathoffset - 4)\n");
        builder.append("                path = ''\n");
        builder.append("                pa = cookie.read(1)\n");
        builder.append("                while unpack('<b', pa)[0] != 0:\n");
        builder.append("                    path = path + pa.decode('utf-8')\n");
        builder.append("                    pa = cookie.read(1)\n");
        builder.append("\n");
        builder.append("                cookie.seek(valueoffset - 4)\n");
        builder.append("                value = ''\n");
        builder.append("                va = cookie.read(1)\n");
        builder.append("                while unpack('<b', va)[0] != 0:\n");
        builder.append("                    value = value + va.decode('utf-8')\n");
        builder.append("                    va = cookie.read(1)\n");
        builder.append("\n");
        builder.append("                yield create_cookie(host, path, cookie_flags, expiry_date, name, value)\n");
        builder.append("\n");
        builder.append("        binary_file.close()\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("def create_cookie(host, path, secure, expires, name, value):\n");
        builder.append("    return cookielib.Cookie(0, name, value, None, False, host, host.startswith('.'), host.startswith('.'), path, True, secure, expires, False, None, None, {})\n");
        builder.append("\n");
        builder.append("def chrome(cookie_file=None):\n");
        builder.append("    return Chrome(cookie_file).load()\n");
        builder.append("\n");
        builder.append("def firefox(cookie_file=None):\n");
        builder.append("    return Firefox(cookie_file).load()\n");
        builder.append("\n");
        builder.append("def safari(cookie_file=None):\n");
        builder.append("    return Safari(cookie_file).load()\n");
        builder.append("\n");
        builder.append("def _get_cookies():\n");
        builder.append("    '''Return all cookies from all browsers'''\n");
        builder.append("    for klass in [Chrome, Firefox]:\n");
        builder.append("        try:\n");
        builder.append("            for cookie in klass().get_cookies():\n");
        builder.append("                yield cookie\n");
        builder.append("        except BrowserCookieError:\n");
        builder.append("            pass\n");
        builder.append("\n");
        builder.append("def load():\n");
        builder.append("    cookie_jar = cookielib.CookieJar()\n");
        builder.append("\n");
        builder.append("    for cookie in sorted(_get_cookies(), key=lambda cookie: cookie.expires):\n");
        builder.append("        cookie_jar.set_cookie(cookie)\n");
        builder.append("\n");
        builder.append("    return cookie_jar\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("# ------------------------------------------------------------------------------\n");
        builder.append("\n");
        builder.append("file_path = os.path.expanduser('~') + os.sep + 'Desktop' + os.sep + 'cookies.txt'\n");
        builder.append("if sys.argv and sys.argv.__len__() > 1:\n");
        builder.append("    file_path = sys.argv[1]\n");
        builder.append("\n");
        builder.append("chrome = chrome()\n");
        builder.append("cookie_list = []\n");
        builder.append("for cookie in chrome:\n");
        builder.append("    cookie_list.append({\n");
        builder.append("        'domain': cookie.domain,\n");
        builder.append("        'name': cookie.name,\n");
        builder.append("        'value': cookie.value,\n");
        builder.append("        'expires': cookie.expires,\n");
        builder.append("        'path': cookie.path\n");
        builder.append("    })\n");
        builder.append("\n");
        builder.append("print json.dumps(cookie_list)\n");
        return builder.toString();
    }

    public static List<CookieDTO> extractChromeCookies() throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(null);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        executor.setStreamHandler(streamHandler);
        String pythonScriptFilePath = Joiner.on(File.separator)
                .join(PathManager.getHomePath(), "cookies", "extract_cookies.py");
        // python 命令写入py文件，文件加运行权限
        File pyFile = new File(pythonScriptFilePath);
        //noinspection ResultOfMethodCallIgnored
        pyFile.getParentFile().mkdirs();
        pyFile.createNewFile();
        FileUtils.write(pyFile, getPythonCommand(), StandardCharsets.UTF_8);
        new DefaultExecutor().execute(CommandLine.parse("chmod 777 " + pythonScriptFilePath), System.getenv());
        // 构建执行的命令
        CommandLine pythonCommand = CommandLine.parse("/usr/bin/env python");
        pythonCommand.addArgument(pythonScriptFilePath);
        log.info("【执行extract_cookies.py】" + pythonCommand);
        executor.execute(pythonCommand, System.getenv());
        String cookieListJson = outputStream.toString(StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(cookieListJson)) {
            log.info("【执行python返回】" + cookieListJson);
        }
        String errorMsg = errorStream.toString(StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(errorMsg)) {
            log.info("【执行python返回Error】" + errorMsg);
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(cookieListJson, new TypeToken<ArrayList<CookieDTO>>() {
        }.getType());
    }

    private static final LoadingCache<String, String> cookiesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(10 * 1000))
            .build(new CacheLoader<>() {
                @Override
                public String load(@NotNull String key) {
                    try {
                        List<CookieDTO> cookieDTOS = extractChromeCookies();
                        Set<String> distinctNameSet = new HashSet<>();
                        return cookieDTOS.stream()
                                .filter(c -> StringUtils.containsIgnoreCase(c.getDomain(), "58corp.com"))
                                .filter(c -> {
                                    if (!names.contains(c.getName())) {
                                        return false;
                                    }
                                    if (StringUtils.equals(c.getName(), "sso_ticket") && !StringUtils.equals(
                                            c.getDomain(), "portal-wconfig.58corp.com")) {
                                        return false;
                                    }
                                    if (distinctNameSet.contains(c.getName())) {
                                        return false;
                                    }
                                    distinctNameSet.add(c.getName());
                                    return true;
                                })
                                .distinct()
                                .map(c -> c.getName() + "=" + c.getValue())
                                .reduce(Joiner.on(";")::join)
                                .orElse("");
                    } catch (Exception e) {
                        log.error("[getCookies error]", e);
                    }
                    return "";
                }
            });

    @Data
    static class CookieDTO {
        private String domain;
        private String name;
        private String value;
        private String expires;
        private String path;
    }


    public static ConfigEntity getNamespace(@Nullable String cluster,
            @Nonnull String group, @Nonnull String namespace) {
        Collection<File> files = FileUtils.listFiles(new File(getWorkspace()),
                new String[]{CommonComponent.PROPERTIES, CommonComponent.JSON_5}, true);
        for (File file : files) {
            String path = file.getPath();
            if (StringUtils.contains(path, group) && StringUtils.contains(path, namespace)) {
                if (StringUtils.isNotBlank(cluster) && StringUtils.contains(path, cluster)) {
                    return CommonComponent.generate2ConfigEntity(file);
                }
                if (StringUtils.isBlank(cluster)) {
                    return CommonComponent.generate2ConfigEntity(file);
                }
            }
        }
        return null;
    }

    public static Settings getSetting() {
        return ApplicationManager.getApplication().getService(Settings.class);
    }

    public static void removeLocalCookiesFile() {
        new File(getWorkspace() + File.separator + "cookies.txt").deleteOnExit();
    }

    public static String getCookies() {
        String cookie = LocalStorage.getSetting().getCookie();
        if (StringUtils.isBlank(cookie)) {
            cookie = cookiesCache.getUnchecked("cookies");
        }
        return cookie;
    }

    public static String getDefaultGroup() {
        return StringUtils.defaultIfBlank(getSetting().getDefaultGroup(), "default_group");
    }

    public static String getWorkspace() {
        // String defaultWs = System.getProperty("user.home") + File.separator + "wconfigws";
        // return StringUtils.defaultIfBlank(getSetting().getWorkspace(), defaultWs);
        return PathManager.getHomePath() + File.separator + "scratches" + File.separator + "wconfigws";
    }

    private static String virtualWorkspace;

    public static String getVirtualWorkspace() {
        return virtualWorkspace;
    }

    public static void setVirtualWorkspace(String path) {
        virtualWorkspace = path;
    }

    public static String getSearchKeys() {
        return getSetting().getSearchKeys();
    }

    public static Set<String> getClusterNameSet() {
        Set<String> ans = new HashSet<>();
        for (String s : getSetting().getClusterKeyName().split(";")) {
            ans.add(s.split(",")[1]);
        }
        return ans;
    }

    public static Set<String> getGroupSet() {
        return new HashSet<>(Arrays.asList(getSetting().getGroupList().split(",")));
    }

    public static String getSelectedGroup() {
        return StringUtils.defaultIfBlank(getSetting().getDefaultGroup(), "default_group");
    }

    public static String getClusterKeyByName(String clusterName) {
        String clusterKeyName = getSetting().getClusterKeyName();
        if (StringUtils.isNotBlank(clusterKeyName)) {
            for (String s : clusterKeyName.split(";")) {
                String[] split = s.split(",");
                if (split.length > 1 && StringUtils.equals(split[1], clusterName)) {
                    return split[0];
                }
            }
        }
        return null;
    }

    public static String getOAName() {
        return getSetting().getOaName();
    }

}
