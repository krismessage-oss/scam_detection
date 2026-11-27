let messages = [], current = "All";
function setStatus(t){document.getElementById("status").textContent=t}
function requestRole(){if(window.Android)Android.requestSmsRole()}
function loadMessages(){
 if(!window.Android)return;
 try{messages=JSON.parse(Android.getMessages());updateStats();render()}
 catch(e){setStatus("Could not load messages: "+e.message)}
}
function updateStats(){
 const n=c=>messages.filter(m=>m.category===c).length;
 document.getElementById("total").textContent=messages.length;
 document.getElementById("spam").textContent=n("Spam");
 document.getElementById("scam").textContent=n("Scam");
 document.getElementById("review").textContent=n("Review")+n("Unclassified");
}
function showMessages(c,b){
 current=c;document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active"));
 if(b)b.classList.add("active");render();
}
function render(){
 const list=document.getElementById("messageList");list.replaceChildren();
 const rows=messages.filter(m=>current==="All"||m.category===current);
 if(!rows.length){const e=document.createElement("div");e.className="empty";e.textContent="No messages in this category.";list.appendChild(e);return}
 rows.forEach(m=>{
  const card=document.createElement("div");card.className="card";
  const row=document.createElement("div");row.className="row";
  const check=document.createElement("input");check.type="checkbox";check.className="pick";check.dataset.id=String(m.id);
  check.disabled=!["Spam","Scam"].includes(m.category);
  const content=document.createElement("div");content.className="content";
  const sender=document.createElement("div");sender.className="sender";sender.textContent=m.sender||"Unknown sender";
  const body=document.createElement("div");body.className="body";body.textContent=m.body||"";
  const badge=document.createElement("span");badge.className="pill "+m.category;badge.textContent=m.category;
  const confidence=document.createElement("span");confidence.className="meta";
  confidence.textContent=m.confidence?" • Heuristic score: "+m.confidence+"%":" • Manual review required";
  const reasons=document.createElement("div");reasons.className="meta";reasons.textContent=(m.reasons||[]).join(", ");
  content.append(sender,body,badge,confidence,reasons);row.append(check,content);card.appendChild(row);list.appendChild(card);
 });
}
function scanMessages(){
 if(!window.Android)return;setStatus("Scanning existing SMS messages…");
 Android.scanExistingMessages();setTimeout(()=>{loadMessages();setStatus("Scan finished. Review all classifications.")},2200);
}
function selectVisible(){document.querySelectorAll(".pick:not(:disabled)").forEach(c=>c.checked=true)}
function deleteSelected(){
 const ids=[...document.querySelectorAll(".pick:checked")].map(c=>Number(c.dataset.id));
 if(!ids.length){setStatus("Select classified Spam or Scam messages first.");return}
 if(!confirm("Permanently delete "+ids.length+" selected Spam/Scam SMS messages?"))return;
 const result=JSON.parse(Android.deleteMessages(JSON.stringify(ids)));
 setStatus(result.error||("Deleted "+result.deleted+" message(s)."));loadMessages();
}
window.addEventListener("load",loadMessages);
