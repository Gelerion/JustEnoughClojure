(ns socks.socks-transfers)

(def sock-varieties
  #{"darned" "argyle" "wool" "horsehair" "mulleted"
    "passive-aggressive" "striped" "polka-dotted"
    "athletic" "business" "power" "invisible" "gollumed"})

(defn sock-count
  [sock-variety count]
  {:variety sock-variety :count count})

(defn generate-sock-gnome
  "Create an initial sock gnome state with no socks"
  [name]
  {:name name :socks #{}})


; Now you can create your actual refs. The gnome will have 0 socks. The dryer,
; on the other hand, will have a set of sock pairs generated from the set of
; sock varieties. Here are our refs:
(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))
(def dryer (ref {:name "LG 1337"
                 :socks (set (map #(sock-count % 2) sock-varieties))}))
;(:socks @sock-gnome)


; Now everything’s in place to perform the transfer. We’ll want to modify the sock-gnome
; ref to show that it has gained a sock and modify the dryer ref to show that it’s lost a sock.
; You modify refs using alter, and you must use alter within a transaction. dosync initiates a
; transaction and defines its extent; you put all transaction operations in its body.
; Here we use these tools to define a steal-sock function, and then call it on our two refs:

(defn steal-sock
  [gnome dryer]
  (dosync
    (when-let [pair (some #(if (= (:count %) 2) %) (:socks @dryer))]
      (let [updated-count (sock-count (:variety pair) 1)]
        (alter gnome update-in [:socks] conj updated-count)
        (alter dryer update-in [:socks] disj pair)
        (alter dryer update-in [:socks] conj updated-count)))))

(steal-sock sock-gnome dryer)
;(:socks @sock-gnome)