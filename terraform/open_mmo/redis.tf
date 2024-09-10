resource "kubernetes_persistent_volume" "redis" {
  metadata {
    name = "redis-pv"
  }
  spec {
    capacity {
      storage = "1Gi"
    }
    access_modes = ["ReadWriteOnce"]
    host_path {
      path = "/mnt/data/redis"
    }
    storage_class_name = "manual"
  }
}

resource "kubernetes_persistent_volume_claim" "redis" {
  metadata {
    name = "redis-pvc"
  }
  spec {
    access_modes = ["ReadWriteOnce"]
    resources {
      requests {
        storage = "1Gi"
      }
    }
    storage_class_name = "manual"
  }
}