function loadRefTagger(i, e, l) {
    var w, E, x, y;

    function r(a) {
        return a.replace(/(\s|\r?\n)+/g, " ").replace(/:/g, ".")
    }

    function z(a) {
        a = (a || "").toLowerCase();
        "default" === a && (a = "");
        return b.lbsBibliaVersionAbbreviations[a] || a
    }

    function F(a, c) {
        var c = z(c),
            b = ["http://biblia.com/bible/", c ? c + "/" : "", a.replace(/:/g, ".")];
        return encodeURI(b.join("").replace(/(\s|\r?\n)+/g, " "))
    }

    function K() {
        var a = /lbsRefTaggerPrefs=(?:((?:\w|\d){2,5})\.(true|false))/.exec(e.cookie),
            c = e.getElementById("lbsRefTaggerCP"),
            d;
        a && (b.lbsBibleVersion = a[1],
            b.lbsAddLogosLink = "true" == a[2]);
        if (null !== c) {
            a = e.getElementById("lbsVersion");
            for (c = 0, d = a.length; c < d; c++) if (a.options[c].outerText == (b.lbsBibleVersion || "default").toUpperCase()) {
                a.selectedIndex = c;
                break
            }
            b.lbsAddLogosLink && (e.getElementById("lbsUseLibronixLinks").checked = "true")
        }
    }

    function L(a, c, b, g) {
        var f = e.createElement("script"),
            A = "cb" + M++,
            o = !1,
            m;
        B[A] = function () {
            clearTimeout(m);
            delete B[A];
            f.parentNode.removeChild(f);
            o || c.apply(null, Array.prototype.slice.call(arguments))
        };
        f.src = a + (/\?/.test(a) ? "&" : "?") + "callback=" + ("Logos.ReferenceTagging.callbacks." + A);
        G.insertBefore(f, G.firstChild);
        g && (m = setTimeout(function () {
            o = !0;
            f.parentNode.removeChild(f);
            b && b()
        }, g))
    }

    function N() {
        clearTimeout(s)
    }

    function O() {
        s = setTimeout(function () {
            q(k)
        }, w)
    }

    function C(a) {
        for (var a = a || i.event, c = a.target || a.srcElement, d, g;
            //  "a" != (c.tagName.toLowerCase();) c = c.parentNode;
             "a" != c.tagName.toLowerCase();) c = c.parentNode;
        d = c.lbsReference;
        g = c.lbsVersion;
        k && (clearTimeout(s), q(k));
        H = setTimeout(function () {
            var a = c,
                e = b.createTooltip(a);
            k && q(k);
            b.populateTooltipContent(e, d, g);
            l.appendChild(e);
            k = e;
            t = a
        }, u ? 1 : E)
    }

    function I() {
        u || (clearTimeout(H), k && (s = setTimeout(function () {
            q(k)
        }, w)))
    }

    function q(a) {
        a && a.parentNode && a.parentNode.removeChild(a);
        k = t = null
    }

    function P(a, c, b) {
        var g = parseInt(a.style.width, 10),
            a = parseInt(a.style.height, 10),
            f = [];
        f.x = b.x + 15;
        f.y = b.y - a;
        if (g > c.width || a > c.height) return f;
        f.x += g;
        f.x > c.width + c.offX - 10 && (f.x = c.width + c.offX - 15 - 10);
        0 > f.x && (f.x = 0);
        f.y < c.offY && (f.y = b.y + a + 25 > c.height + c.offY ? c.offY : b.y + 25);
        f.x -= g + 3;
        return f
    }

    function Q(a) {
        var c = {
                x: 0,
                y: 0
            },
            b = a,
            g = 0,
            f = 0;
        if ("number" === typeof a.offsetLeft) {
            for (; a;) c.x += a.offsetLeft, c.y += a.offsetTop, a = a.offsetParent;
            for (; b && b !== l && b !== e.documentElement;) f += b.scrollTop || 0, g += b.scrollLeft || 0, b = b.parentNode;
            c.x -= g;
            c.y -= f
        } else a.x && (c.x = a.x, c.y = a.y);
        return c
    }

    function R() {
        var a = {};
        if ("number" === typeof i.innerHeight) a.width = i.innerWidth, a.height = i.innerHeight;
        else if (e.documentElement && (e.documentElement.clientHeight || e.documentElement.clientWidth)) a.width = e.documentElement.clientWidth, a.height = e.documentElement.clientHeight;
        else if (l && (l.clientWidth || l.clientHeight)) a.width = l.clientWidth, a.height = l.clientHeight;
        var b = [];
        b.offX = i.pageXOffset || l.scrollLeft || e.documentElement.scrollLeft;
        b.offY = i.pageYOffset || l.scrollTop || e.documentElement.scrollTop;
        b && (a.offX = b.offX, a.offY = b.offY);
        return a
    }

    var B = {},
        M = 0,
        G = e.getElementsByTagName("head")[0],
        H = null,
        s = null,
        k = null,
        t = null,
        u = !1,
        D = {};
    x = 0;
    y = 0;
    E = 250;
    w = 300;
    var J = !1,
        b = {
            lbsBibleVersion: "ESV",
            lbsLibronixBibleVersion: "",
            lbsLogosBibleVersion: "",
            lbsAddLibronixDLSLink: !1,
            lbsAddLogosLink: !1,
            lbsAppendIconToLibLinks: !1,
            lbsAppendIconToLogosLinks: !1,
            lbsLibronixLinkIcon: null,
            lbsLogosLinkIcon: "dark",
            lbsUseTooltip: !0,
            lbsLinksOpenNewWindow: !1,
            lbsNoSearchTagNames: ["h1", "h2", "h3"],
            lbsNoSearchClassNames: [],
            lbsRootNode: null,
            lbsCssOverride: !1,
            lbsCaseInsensitive: !1,
            lbsConvertHyperlinks: !1,
            lbsHyperlinkTestList: [],
            lbsMaxTreeDepth: 200,
            callbacks: B,
            insertRefNode: function (a, c, d, g) {
                var a = r(a),
                    d = d || b.lbsBibleVersion,
                    f = b.addLinkAttributes(e.createElement("a"), a, z(d));
                f.innerHTML = c;
                g.parentNode.insertBefore(f, g);
                b.lbsAddLogosLink && b.insertLibLink(g, a.replace(/(\d)\s*(?:[a-z]|ff)(\W|$)|/g, "$1$2").replace(/\s+/g, "").replace(/[\u2012\u2013\u2014\u2015]+/g, "-"), d);
                x++
            },
            addLinkAttributes: function (a, c, d) {
                var g = a.innerHTML;
                a.href = F(c, d);
                a.innerHTML = g;
                a.lbsReference = c;
                a.lbsVersion = d;
                a.className = a.className && 0 < a.className.length ? a.className + " lbsBibleRef" : "lbsBibleRef";
                a.setAttribute("data-reference", c);
                a.setAttribute("data-version", d);
                b.lbsLinksOpenNewWindow && (a.target = "_blank");
                b.lbsUseTooltip && (a.addEventListener ? (a.addEventListener("mouseover", C, !1), a.addEventListener("mouseout", I, !1),
                    a.addEventListener("click", function (a) {
                        u && a.target !== t && (a.preventDefault(), null == t && C.call(this, a))
                    })) : a.attachEvent && (a.attachEvent("onmouseover", C), a.attachEvent("onmouseout", I)));
                return a
            },
            insertLibLink: function (a, c) {
                var d = e.createElement("img"),
                    g, f;
                d.src = "light" === (b.lbsLibronixLinkIcon || b.lbsLogosLinkIcon).toLowerCase() ? e.location.protocol + "//www.logos.com/images/Corporate/LibronixLink_light.png" : e.location.protocol + "//www.logos.com/images/Corporate/LibronixLink_dark.png";
                d.border = 0;
                d.title = "Open in Logos Bible Software (if available)";
                d.style.marginLeft = "4px";
                d.style.marginBottom = "0px";
                d.style.marginRight = "0px";
                d.style.border = 0;
                d.style.padding = 0;
                d.style["float"] = "none";
                d.align = "bottom";
                c ? (g = e.createElement("a"), g.href = ["libronixdls:keylink|ref=[en]bible:", c].join(""), f = b.lbsLogosBibleVersion || b.lbsLibronixBibleVersion, f.length && "DEFAULT" !== f.toUpperCase() && (g.href += "|res=LLS:" + f.toUpperCase()), g.className = "lbsLibronix", g.appendChild(d), a.parentNode.insertBefore(g, a)) : a.appendChild(d)
            },
            insertTextNode: function (a, b) {
                var d = e.createTextNode(a);
                b.parentNode.insertBefore(d, b)
            },
            refSearch: function (a, c, d, g, f, e) {
                var o = 0,
                    m = d,
                    j = g,
                    h, i, k, l, n = b.lbsBibleVersion,
                    p = null,
                    v = RegExp.rightContext;
                if (d && (h = b.lbsBookContRegExp.exec(a))) v = RegExp.rightContext, k = [m, " ", h[2]].join(""), j = h[3], l = h[1];
                if (g && !h && (h = b.lbsChapContRegExp.exec(a))) v = RegExp.rightContext, k = [m, " ", j, ":", h[2]].join(""), l = h[1];
                if (!h && b.lbsRefQuickTest.test(a) && (h = b.lbsRefRegExp.exec(a))) d = RegExp.leftContext, i = v = RegExp.rightContext, k = h[2],
                    l = d + h[1], m = h[3], j = h[4], h[9] && (p = h[9], n = p.replace(/\W/g, "")), h[8] && (m = h[8], j = 1);
                h ? e ? (a = r(k), n = n || b.lbsBibleVersion, b.addLinkAttributes(e, a, n)) : (i || (i = v), b.insertTextNode(l, c), b.insertRefNode(k, null === p ? h[2] : h[2] + p, n, c), o = b.refSearch(i, c, m, j, n == b.lbsBibleVersion ? null : n), o += b.lbsAddLogosLink ? 3 : 2) : a !== c.nodeValue && (a && f != a && b.insertTextNode(a, c), c.parentNode.removeChild(c));
                return o
            },
            traverseDom: function (a, c, d) {
                var d = d || 0,
                    g = 0,
                    f = !1,
                    e = (a.tagName || "").toLowerCase(),
                    o = a.className ? a.className.split(" ") : [],
                    m = !1,
                    j, h, k, i, l, n, p;
                if (d > b.lbsMaxTreeDepth) return 0;
                for (j = 0, l = b.lbsNoSearchClassNames.length; j < l; j++) {
                    for (n = 0, p = o.length; n < p; n++) if (b.lbsNoSearchClassNames[j].toLowerCase() == o[n].toLowerCase()) {
                        m = !0;
                        break
                    }
                    if (m) break
                }
                if (3 === a.nodeType) g = b.refSearch(a.nodeValue, a, null, null, null, c);
                else if (0 < e.length && (!b.lbsNoSearchTags[e] || "a" === e) && !m) {
                    c = null;
                    if ("a" === e) {
                        j = /^libronixdls:/i;
                        if (j.test(a.href))(b.lbsAppendIconToLibLinks || b.lbsAppendIconToLogosLinks) && (!a.lastChild || !(a.lastChild.tagName && "img" === a.lastChild.tagName.toLowerCase())) && b.insertLibLink(a, null);
                        else if (/lbsBibleRef/i.test(a.className)) i = a.getAttribute("data-reference"), j = a.getAttribute("data-version"), i && b.addLinkAttributes(a, r(i), j || b.lbsBibleVersion);
                        else if (/bibleref/i.test(a.className)) f = b.tagBibleref(a, function (a, c, d) {
                            h = r(c);
                            k = d || b.lbsBibleVersion;
                            b.addLinkAttributes(a, h, k)
                        });
                        else if (!0 === b.lbsConvertHyperlinks && 1 === a.childNodes.length && 3 === a.firstChild.nodeType) {
                            j = 0 === b.lbsHyperlinkTestList.length;
                            for (i in b.lbsHyperlinkTestList) if (0 <= a.href.toLowerCase().indexOf(i.toLowerCase())) {
                                j = !0;
                                break
                            }
                            j && (c = a)
                        }
                        if (null === c) return g
                    }
                    "cite" === e && /bibleref/.test(a.className.toLowerCase()) && (f = b.tagBibleref(a, function (a, c, d) {
                        b.insertRefNode(c, a.innerHTML, d, a.firstChild);
                        a.removeChild(a.lastChild)
                    }));
                    if (!f) {
                        a = a.childNodes;
                        for (j = 0; j < a.length;) f = b.traverseDom(a[j], c, d + 1), j += f + 1
                    }
                }
                return g
            },
            tagBibleref: function (a, b) {
                var d = !1,
                    e, f;
                y++;
                a.title && 1 >= a.childNodes.length && (d = /^([A-Z]{2,5})[\s:]/.exec(a.title), f = RegExp.rightContext, d ? e = d[1] : f = a.title, b(a, f, e), d = !0);
                return d
            },
            createTooltip: function (a) {
                var b = Q(a),
                    a = e.createElement("div"),
                    d = R();
                a.style.position = "absolute";
                a.style.width = "350px";
                a.style.height = "150px";
                a.style.zIndex = "9999999";
                a.className = "lbsTooltip";
                b = P(a, d, b);
                // Customized here
                var winW = 630;
                if (document.body && document.body.offsetWidth) {
                    winW = document.body.offsetWidth;
                }
                if (document.compatMode == 'CSS1Compat' &&
                    document.documentElement &&
                    document.documentElement.offsetWidth) {
                    winW = document.documentElement.offsetWidth;
                }
                if (window.innerWidth && window.innerHeight) {
                    winW = window.innerWidth;
                }
                if (winW > 330) {
                    a.style.top = b.y + "px";
                    a.style.left = b.x + "px";
                } else {
                    a.style.width = "320px";
                    a.style.top = b.y + "px";
                    a.style.left = 1 + "px";
                }
                a.onmouseover = N;
                a.onmouseout = O;
                a.addEventListener && a.addEventListener("touchstart", function (a) {
                    a.stopPropagation()
                }, !1);
                return a
            },
            populateTooltipContent: function (a, c, d) {
                function g(b, c) {
                    var d = encodeURIComponent,
                        c = z(c);
                    return [e.location.protocol, "//biblia.com/bible/", c ? d(c) + "/" : "", d(b), "?target=reftagger&userData=",
                        d(a.id)].join("")
                }

                var f = a.currentStyle ? a.currentStyle.backgroundColor : "inherit";
                a.innerHTML = b.constructTooltipContent(f, "Loading...", "", "");
                (function (a, b, c, d) {
                    var e = a + "-" + b;
                    D.hasOwnProperty(e) ? c(D[e]) : L(g(a, b), function (a) {
                        D[e] = a;
                        c(a)
                    }, d, 5E3)
                })(c, d, function (c) {
                    a.innerHTML = b.constructTooltipContent(a.currentStyle ? a.currentStyle.backgroundColor : "inherit", c.reference + " (" + c.version + ")", c.content.replace('<span class="verse-ref" />', ""), '<div style="float: left; margin-left: 8px;"><a href="' + F(c.reference,
                        c.resourceName) + '" target="_blank">More &raquo;</a></div>').replace(/\<span\s*class="verse-ref"\s*\/>/gi, "")
                }, function () {
                    a.innerHTML = b.constructTooltipContent(f, "Sorry", "<p>This reference could not be loaded at this time.</p>", "")
                })
            },
            constructTooltipContent: function (a, b, d, g) {
                // Customized here
                var winW = 630;
                if (document.body && document.body.offsetWidth) {
                    winW = document.body.offsetWidth;
                }
                if (document.compatMode == 'CSS1Compat' &&
                    document.documentElement &&
                    document.documentElement.offsetWidth) {
                    winW = document.documentElement.offsetWidth;
                }
                if (window.innerWidth && window.innerHeight) {
                    winW = window.innerWidth;
                }

                if (winW > 330) {
                    return '<div style="position: absolute; background: transparent url(' + e.location.protocol + '//bible.logos.com/content/images/refTaggerDropShadow.png) no-repeat; width: 364px; height: 164px; left: -7px; top: -7px; z-index: -1"></div><div class="lbsContainer" style="height:150px; background-color:' + a + ';"><div class="lbsTooltipHeader">' + b + '</div><div class="lbsTooltipBody" style="width:335px;">' + d + '</div><div class="lbsTooltipFooter" style="width:345px;">' + g + '<div><a href="http://www.logos.com/reftagger" target="_blank">Powered by RefTagger</a></div></div></div>'
                } else {
                    return '<div style="position: absolute; background: transparent url(' + e.location.protocol + '//bible.logos.com/content/images/refTaggerDropShadow.png) no-repeat; width: 334px; height: 164px; left: -7px; top: -7px; z-index: -1"></div><div class="lbsContainer" style="height:150px; background-color:' + a + ';"><div class="lbsTooltipHeader">' + b + '</div><div class="lbsTooltipBody" style="width:305px;">' + d + '</div><div class="lbsTooltipFooter" style="width:315px;">' + g + '<div><a href="http://www.logos.com/reftagger" target="_blank">Powered by RefTagger</a></div></div></div>'
                }
            },
            appendCssRules: function () {
                if (!e.getElementById("lbsToolTipStyle")) {
                    var a = e.createElement("link");
                    a.type = "text/css";
                    a.rel = "stylesheet";
                    a.href = e.location.protocol + "//bible.logos.com/Content/ReferenceTagging.css";
                    a.media = "screen";
                    a.id = "lbsToolTipStyle";
                    e.getElementsByTagName("head")[0].insertBefore(a, e.getElementsByTagName("head")[0].firstChild)
                }
            },
            lbsSavePrefs: function () {
                var a = e.getElementById("lbsRefTaggerCP"),
                    b = e.getElementById("lbsVersion").value,
                    d = !!e.getElementById("lbsUseLibronixLinks").checked,
                    g = new Date;
                a && (g.setFullYear(g.getFullYear() + 10), e.cookie = "lbsRefTaggerPrefs=" + b + "." + d + ";expires=" + g.toGMTString() + ";path=/", i.location.reload())
            },
            Init: function () {
                if (!b.Initialized) {
                    var a, c;
                    e.addEventListener && e.addEventListener("touchstart",

                        function (a) {
                            u = !0;
                            !/lbsBibleRef/i.test(a.target.className) && k && q(k)
                        }, !1);
                    b.lbsCssOverride || b.appendCssRules();
                    K();
                    b.lbsNoSearchTags = {
                        applet: !0,
                        hr: !0,
                        head: !0,
                        img: !0,
                        input: !0,
                        meta: !0,
                        script: !0,
                        select: !0,
                        textarea: !0
                    };
                    for (c in b.lbsNoSearchTagNames) a = b.lbsNoSearchTagNames[c], b.lbsNoSearchTags[a] = !0;
                    b.lbsNoSearchClasses = {};
                    for (c in b.lbsNoSearchClassNames) b.lbsNoSearchClasses[b.lbsNoSearchClassNames[c]] = !0;
                    b.lbsBibliaVersionAbbreviations = {
                        dar: "darby",
                        nasb: "nasb95",
                        gw: "godsword",
                        kjv21: "kjv1900",
                        nivuk: "niv",
                        kar: "hu-bible",
                        byz: "byzprsd",
                        kjv: "kjv1900",
                        net: "gs-netbible"
                    };
                    a = "AB,ASV,CEV,DARBY,DAR,ESV,GW,HCSB,KJ21,KJV,NASB,NCV,NET,NIRV,NIV,NIVUK,NKJV,NLT,NLV,MESSAGE,TNIV,WE,WNT,YLT,TNIV,NIRV,TNIV,NASB,WESTCOTT,CHASAOT,STEPHENS,AV 1873,KJV APOC,ELZEVIR,IT-DIODATI1649,TISCH,TISCHENDORF,CS-KR1579,TR1881,TR1894MR,TR1550MR,KAR,BYZ,LEB".split(",");
                    b.lbsRefQuickTest = RegExp("((\\d{1,3})(?:\\s?\\:\\s?|\\.)(\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?))|(Ob(?:ad(?:iah)?)?|Ph(?:ilem(?:on)?|m)|(?:(?:2(?:nd\\s)?|[Ss]econd\\s|II\\s)|(?:3(?:rd\\s)?|[Tt]hird\\s|III\\s))\\s*J(?:o(?:hn?)?|h?n)|Jude?)", "i");
                    b.lbsRefRegExp = RegExp("(\\W|^)((Z(?:e(?:p(?:h(?:aniah)?)?|c(?:h(?:ariah)?)?)|[pc])|W(?:is(?:d(?:om(?:\\s+of\\s+(?:Ben\\s+Sirah?|Solomon))?|.?\\s+of\\s+Sol))?|s)|T(?:ob(?:it)?|it(?:us)?|he(?:\\s+(?:Song\\s+of\\s+(?:Three\\s+(?:Youth|Jew)s|the\\s+Three\\s+Holy\\s+Children)|Re(?:velation|st\\s+of\\s+Esther))|ssalonians)|b)|S(?:us(?:anna)?|o(?:ng(?:\\s+(?:of\\s+(?:Thr(?:ee(?:\\s+(?:(?:Youth|Jew)s|Children))?)?|So(?:l(?:omon)?|ngs)|the\\s+Three\\s+Holy\\s+Children)|Thr))?)?|ir(?:a(?:c?h)?)?|OS)|R(?:u(?:th)?|o(?:m(?:ans)?)?|e(?:v(?:elation)?|st\\s+of\\s+Esther)?|[vm]|th)|Qoh(?:eleth)?|P(?:s(?:\\s+Sol(?:omon)?|a(?:lm(?:s(?:\\s+(?:of\\s+)?Solomon)?)?)?|Sol|s|l?m)?|r(?:ov(?:erbs)?|\\s+(?:(?:of\\s+)?Man|Az)|ayer\\s+of\\s+(?:Manasse[sh]|Azariah)|v)?|h(?:il(?:em(?:on)?|ippians)?|[pm])|Ma)|O(?:b(?:ad(?:iah)?)?|des)|N(?:u(?:m(?:bers)?)?|e(?:h(?:emiah)?)?|a(?:h(?:um)?)?|[mb])|M(?:rk?|ic(?:ah)?|a(?:t(?:t(?:hew)?)?|l(?:achi)?|r(?:k))|[tlk])|L(?:uke?|e(?:v(?:iticus)?|t(?:ter\\s+of\\s+Jeremiah|\\s+Jer))?|a(?:od(?:iceans)?|m(?:entations)?)?|[vk]|tr\\s+Jer|Je)|J(?:ud(?:g(?:es)?|ith|e)?|o(?:s(?:h(?:ua)?)?|n(?:ah)?|el?|hn|b)|nh?|e(?:r(?:emiah)?)?|d(?:th?|gs?)|a(?:me)?s|[ts]h|[rmlgb]|hn)|Is(?:a(?:iah)?)?|H(?:o(?:s(?:ea)?)?|e(?:b(?:rews)?)?|a(?:g(?:gai)?|b(?:akkuk)?)|g)|G(?:e(?:n(?:esis)?)?|a(?:l(?:atians)?)?|n)|E(?:z(?:ra?|e(?:k(?:iel)?)?|k)|x(?:o(?:d(?:us)?)?)?|s(?:th(?:er)?)?|p(?:ist(?:le\\s+(?:to\\s+(?:the\\s+)?Laodiceans|Laodiceans)|\\s+Laodiceans)|h(?:es(?:ians)?)?|\\s+Laod)?|c(?:cl(?:es(?:iast(?:icu|e)s)?|us)?)?|noch)|D(?:eut(?:eronomy)?|a(?:n(?:iel)?)?|[tn])|C(?:ol(?:ossians)?|anticle(?:\\s+of\\s+Canticle)?s)|B(?:el(?:\\s+and\\s+the\\s+Dragon)?|ar(?:uch)?)|A(?:m(?:os)?|dd(?:\\s+(?:Ps(?:alm)?|Es(?:th)?)|ition(?:s\\s+to\\s+Esther|al\\s+Psalm)|Esth)|c(?:(?:t)s)?|zariah|Es)|\u03c8|(?:4(?:th\\s)?|[Ff]ourth\\s|(?:IIII|IV)\\s)\\s*(?:Ma(?:c(?:c(?:abees)?)?)?)|(?:3(?:rd\\s)?|[Tt]hird\\s|III\\s)\\s*(?:Ma(?:c(?:c(?:abees)?)?)?|Jo(?:h(?:n)?)?|Jn\\.?|Jhn)|(?:(?:2(?:nd\\s)?|[Ss]econd\\s|II\\s)|(?:1(?:st\\s)?|[Ff]irst\\s|I\\s))\\s*(?:T(?:i(?:m(?:othy)?)?|h(?:es(?:s(?:alonians)?)?)?)|S(?:a(?:m(?:uel)?)?|m)?|P(?:e(?:t(?:er)?)?|t)|Ma(?:c(?:c(?:abees)?)?)?|K(?:i(?:n(?:gs)?)?|gs)|J(?:o(?:hn?)?|h?n)|Es(?:d(?:r(?:as)?)?)?|C(?:o(?:r(?:inthians)?)?|h(?:r(?:on(?:icles)?)?)?)))(?:\\.?\\s*(\\d{1,3})(?:\\s?\\:\\s?|\\.)(\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?)(\\s?(?:-|--|\\u2013|\\u2014)\\s?\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?((?:\\s?\\:\\s?|\\.)\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?)?(?!\\s*(?:T(?:i(?:m(?:othy)?)?|h(?:es(?:s(?:alonians)?)?)?)|S(?:a(?:m(?:uel)?)?|m)?|P(?:e(?:t(?:er)?)?|t)|Ma(?:c(?:c(?:abees)?)?)?|K(?:i(?:n(?:gs)?)?|gs)|J(?:o(?:hn?)?|h?n)|Es(?:d(?:r(?:as)?)?)?|C(?:o(?:r(?:inthians)?)?|h(?:r(?:on(?:icles)?)?)?))(?:\\W)))?)|(Ob(?:ad(?:iah)?)?|Ph(?:ilem(?:on)?|m)|(?:(?:2(?:nd\\s)?|[Ss]econd\\s|II\\s)|(?:3(?:rd\\s)?|[Tt]hird\\s|III\\s))\\s*J(?:o(?:hn?)?|h?n)|Jude?)\\s*\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?(?:\\s?(?:-|--|\\u2013|\\u2014)\\s?\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?)?)([,]?\\s?(?:" + a.join("|") + ")|[,]?\\s?[(](?:" + a.join("|") + ")[)])?", b.lbsCaseInsensitive ? "i" : "");
                    b.lbsBookContRegExp = RegExp("^((?:(?:[,;\\.]+)?\\s?(?:and|or|&|&amp;)?)\\s*(?:(?:(?:cf|Cf|CF)[.,]?\\s?(?:v(?:v|ss?)?[.]?)?)[.,]?\\s*)?)((\\d{1,3})(?:\\s?\\:\\s?|\\.)\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?(?:\\s?(?:-|--|\\u2013|\\u2014)\\s?\\d{1,3}(?:(?:\\s?\\:\\s?|\\.)\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?)?)?)");
                    b.lbsChapContRegExp = RegExp("^((?:(?:[,;\\.]+)?\\s?(?:and|or|&|&amp;)?)\\s*(?:(?:(?:cf|Cf|CF)[.,]?\\s?(?:v(?:v|ss?)?[.]?)?)[.,]?\\s*)?)(\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?(?:\\s?(?:-|--|\\u2013|\\u2014)\\s?\\d{1,3}(?:(?:\\s?(?:[a-z]|ff))(?=\\W|$))?)?)(?!\\s*(?:st|nd|rd|th|T(?:i(?:m(?:othy)?)?|h(?:es(?:s(?:alonians)?)?)?)|S(?:a(?:m(?:uel)?)?|m)?|P(?:e(?:t(?:er)?)?|t)|Ma(?:c(?:c(?:abees)?)?)?|K(?:i(?:n(?:gs)?)?|gs)|J(?:o(?:hn?)?|h?n)|Es(?:d(?:r(?:as)?)?)?|C(?:o(?:r(?:inthians)?)?|h(?:r(?:on(?:icles)?)?)?)))",
                        b.lbsCaseInsensitive ? "i" : "");
                    b.Initialized = !0
                }
            },
            tag: function (a, c) {
                b.lbsAddLogosLink = b.lbsAddLogosLink || b.lbsAddLibronixDLSLink;
                "ab".match(/b/);
                e.getElementById && e.childNodes && e.createElement && RegExp.leftContext && (b.Initialized || b.Init(), b.traverseDom(a || b.lbsRootNode || l), J || ((new Image).src = [e.location.protocol, "//bible.logos.com/util/ReferenceData.aspx?location=", encodeURIComponent(e.location), "&refCount=", +x, "&microrefCount=", +y, "&bibleVersion=", encodeURIComponent(b.lbsBibleVersion), "&libronix=", !!b.lbsAddLogosLink, "&tooltip=", !!b.lbsUseTooltip, "&source=", encodeURIComponent(c || ""), "&rand=", Math.random().toString().substring(10)].join(""), J = !0))
            }
        };
    i.Logos = i.Logos || {};
    i.Logos.ReferenceTagging = i.Logos.ReferenceTagging || b
}
