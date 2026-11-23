// Glow Quest - service worker
// A CHAQUE MISE A JOUR : changer VERSION (et APP_VERSION dans index.html)
const VERSION = "1.9.1";
const CACHE = "glowquest-" + VERSION;
const FILES = ["./", "./index.html", "./manifest.json", "./icon-192.png", "./icon-512.png"];

self.addEventListener("install", e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES.map(u => new Request(u, { cache: "reload" })))));
});

self.addEventListener("activate", e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE && k !== "gq-config").map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("message", e => {
  if (e.data === "skip") self.skipWaiting();
});

// Hors ligne : on sert le cache. Les nouveautés arrivent via un nouveau VERSION.
self.addEventListener("fetch", e => {
  if (e.request.method !== "GET") return;
  e.respondWith(caches.match(e.request, { ignoreSearch: true }).then(r => r || fetch(e.request)));
});

// ---------- Rappels ----------
const CFG="gq-config";
async function getCfg(){const c=await caches.open(CFG);const r=await c.match("cfg");return r?r.json():{on:false,times:[]}}
async function setCfg(v){const c=await caches.open(CFG);await c.put("cfg",new Response(JSON.stringify(v)))}
self.addEventListener("message",e=>{if(e.data&&e.data.type==="rem")e.waitUntil(getCfg().then(old=>setCfg(Object.assign({},old,e.data.cfg))))});
const MSG=["Tes missions du jour t'attendent !","Une petite quête avant de souffler ?","Ta série compte sur toi !","Encore quelques points pour valider ta journée !","Ton perso a hâte de gagner des pièces !"];
self.addEventListener("periodicsync",e=>{if(e.tag!=="gq-rem")return;e.waitUntil((async()=>{
  const cfg=await getCfg();if(!cfg.on||!cfg.times||!cfg.times.length)return;
  const now=new Date(),day=now.toDateString(),mins=now.getHours()*60+now.getMinutes();
  let due=-1;cfg.times.forEach((t,i)=>{const[h,m]=t.split(":").map(Number);if(mins>=h*60+m)due=i});
  if(due<0)return;const key=day+"#"+due;if(cfg.last===key)return;
  await setCfg(Object.assign({},cfg,{last:key}));
  await self.registration.showNotification("Glow Quest",{body:MSG[due%MSG.length],icon:"icon-192.png",badge:"icon-192.png",tag:"gq-rem"});
})())});
self.addEventListener("notificationclick",e=>{e.notification.close();e.waitUntil(clients.matchAll({type:"window",includeUncontrolled:true}).then(l=>{for(const c of l)if("focus" in c)return c.focus();return clients.openWindow("./")}))});
