(ns soundboardloop)

(def $ js/$)

(defn log [text]
  (.log js/console (pr-str text)))
(defn timeout [frequency function]
  (.setTimeout js/window
               function
               frequency))

(defrecord Sound [el])

(defn copy-sound [sound]
  (let [copy (-> sound :el $ (.clone true) (.appendTo "body"))]
    (Sound. copy)))

(defn audio-element [sound] 
  (-> ($ (:el sound)) (.children "audio") .first $))

(defn filename [sound]
  (-> ($ (:el sound)) (.data "filename")))

(defn play [sound] 
  (let [fp (fn [_ el] 
             (.load el)
             (.play el))]
    (-> sound audio-element (.each fp))))

(defprotocol SoundEnum
  "Returns the next sound"
  (nextSound [this] "Returns the next sound and new sound enum"))

(def pause (Sound. nil))

(defrecord Beat [sound]
  SoundEnum
  (nextSound [this] {:sound sound :enum nil}))
(def beat? #(instance? Beat %))

(defrecord Loop [beats]
  SoundEnum
  (nextSound [this]
    (let [[b & bs] beats
          {sound :sound} b]
      {:sound sound :enum (Loop. (concat bs [b]))})))

(defn copy-beat [{sound :sound}]
  (Beat. (copy-sound sound)))

(defn copy-loop [{beats :beats}]
  (Loop. (map copy-beat beats)))

(def loop? #(instance? Loop %))

(defn play-loop [loop]
  (let [{s :sound n :enum} (nextSound loop)]
    (play s)
    ;(log (filename s))
    (timeout 1000 #(play-loop n))))


(def all-sounds (->> ($ ".sounds > .sound")
                  .toArray
                  (map #(Sound. %))))
(log all-sounds)
(log (first all-sounds))
(def first-sound (first all-sounds))
;(log (audio-element first-sound))
;(play first-sound)

(def default-sounds (map #(Sound. %)
                         (take 20 (range))))
(def loop-all (Loop. (map #(Beat. (copy-sound %)) all-sounds)))
(def loop-all2 (copy-loop loop-all))

(defn loop-from-el [loop-el]
  (let [els (-> loop-el $ (.children ".touch-sound") .toArray)
        sounds (map #(Sound. %) els)]
    sounds))

(defn loop1 [] (loop-from-el ".loop:first"))

(defn setup-drag-drop []
  (log "setting up drag and drop")
  (defn prevent [e]
    (if (.-preventDefault e)
      (.preventDefault e)))
  (defn stop [e]
    (if (.-stopPropagation e)
      (.stopPropagation e)))

  ;(defn handle-dragover [e]
    ;(prevent e)
    ;false)

  ;(defn handle-dragstart [e]
    ;(log "drag start")
    ;(stop e)
    ;(let [dt (-> e .-originalEvent .-dataTransfer)
          ;html (-> e .-currentTarget .-outerHTML )]
      ;(.log js/console dt)
      ;(set! (.-effectAllowed dt) "copy")
      ;(.setData dt "text/html" html)
      ;(log (str "Set html to " html))))

  ;(defn handle-drop [e]
    ;(log "drop")
    ;(log e)
    ;(stop e)
    ;(let [html (-> e .-originalEvent .-dataTransfer (.getData "text/html"))]
      ;(this-as this
               ;(-> ($ this) (.append html))))
    ;false)

  ;(defn handle-dragend [e]
    ;(stop e)
    ;false)

  ;(defn on [event selector fun]
    ;(-> ($ js/document) (.on event selector fun)))

  ;(defn on-loop [event fun]
    ;(on event ".loop" fun))

  (defn sortables []
    (-> ".loop" $))

  (defn refresh-sortables []
    (-> (sortables) (.sortable "refresh")))

  (defn cancel-button []
    (-> "<div>X</div>" $ (.addClass "delete")))

  (defn on-drop [event ui]
    ;(prevent event)
    ;(stop event)
    (.log js/console ui)
    (.log js/console event)
    (let [t (-> event .-target $)
          p (-> ui .-draggable .parent $)]
      ;(refresh-sortables)
      (log "on drop")
      (if (-> p (.is ".loop"))
        (log "parent is loop so don't do anything")
        (let [h (-> ui .-helper $ .clone 
                  (.removeAttr "style") (.removeClass "draggable")
                  (.removeClass "drag-helper")
                  (.append (cancel-button)))]
          (-> t (.append h)))))
    (log "loop1")
    (log (loop1))
    (.log js/console (loop1)))

  ;(.log js/console (.-draggable ($ ".touch-sound")))
  (-> ".loop" $ (.droppable #js {:drop on-drop}))
  (-> "#soundboard .touch-sound" $ 
    (.draggable #js {
                     :helper (fn [e] 
                               (.log js/console e)
                               (-> e .-target $ .clone (.addClass "drag-helper")))
                     :start (fn []
                              (log "start")
                              (-> ".loop" $ (.addClass "drop-target")))
                     :stop (fn []
                             (log "stop")
                             (-> ".loop" $ (.removeClass "drop-target")))}))
  (let [s (sortables)]
    (.sortable s)
    (.disableSelection s))

  (defn delete-loop-sound [e]
    (-> e .-target $ .parent .remove)
    (.log js/console e))

  (-> js/document $ (.on "click" ".touch-sound > .delete" delete-loop-sound))

  ;(on "drop" ".touch-sound-placeholder" handle-drop)
  ;(on-loop "drop" handle-drop)
  ;(on-loop "dragover" handle-dragover)
  ;(on-loop "dragend" handle-dragend)
  ;(on "dragstart" ".touch-sound" handle-dragstart)
  )

(defn ready []
  (setup-drag-drop)
  (log "ready"))

($ ready)

;(def loop-1 (Loop. (->> default-sounds (drop 5) (take 3))))
;(def loop-2 (Loop. (->> default-sounds (drop 10) (take 5))))
;(def loop-3 (Loop. (list loop-1 loop-2)))

;(log pause)
;(log loop-1)
;(log loop-2)
;(log loop-3)
;(log (map loop? (:beats loop-3)))
;(log default-sounds)
;(timeout 1000 #(log "hello"))
;(play-loop loop-all)
;(timeout 1000 #(play-loop loop-all2))

