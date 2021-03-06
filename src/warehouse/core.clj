(ns warehouse.core
  (:gen-class)
  (:require [clojure.core.async :as async :refer [>! <! >!! <!! go chan buffer sliding-buffer close! alts!!]]))

(def warehouse-capacity 10)
(def warehouse-channel (chan warehouse-capacity))
(def banana-channel (chan (sliding-buffer 3)))

(doseq [item (range 3)]  
 (>!! banana-channel :bananas))

(def stock-map {0 :shirt
                1 :pants
                2 :socks
                3 :shoes})

(defn generate-random-items [warehouse-capacity]
  (let [items (for [x (range warehouse-capacity)] (rand-int (count (keys stock-map))))]
    (map #(get stock-map %) items)))

(generate-random-items 10)

(defn load-items-channel [items channel]
  (map #(>!! channel %) items))

(defn make-payment-channel []
  (let [payments (chan)]
    (go
      (while true
       (let [in (<! payments)]
         (if (number? in)
           (let [[item ch] (alts!! [warehouse-channel banana-channel])]
             (println "channel: " ch)
             (println "item: " item))
           (println "We only accept numeric values! No Number, No Clothes")))))
   payments))

(def income (make-payment-channel))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (load-items-channel (generate-random-items warehouse-capacity) warehouse-channel))




