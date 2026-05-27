# Mobile Dashboard Tổng quan (Fanpage Care)

## 1) Data model đề xuất

### Bảng `service_packages`
- `code`: BASIC_15 | PRO_15 | BASIC_30 | PRO_30
- `label`: Gói 15 cơ bản, Gói 15 cao cấp...
- `quota_posts`, `quota_images`, `quota_videos`

### Bảng `subscription_progress`
- `subscription_id`
- `completed_posts`
- `completed_images`
- `completed_videos`

### Bảng `service_definitions`
- `id`, `name`, `icon`, `tier_scope`
- `progress_mode`: `QUANTITY` | `STATUS`
- `quota_key`: `POSTS` | `IMAGES` | `VIDEOS` | null

### Bảng `subscription_service_progress`
- `subscription_id`
- `service_id`
- `percent` (dành cho dịch vụ `STATUS`)

## 2) Quy tắc map dịch vụ theo gói

### Gói cơ bản
- Viết bài (`posts`, QUANTITY, POSTS)
- Thiết kế hình ảnh (`design`, QUANTITY, IMAGES)

### Gói cao cấp
- Viết bài (`posts`, QUANTITY, POSTS)
- Thiết kế hình ảnh (`design`, QUANTITY, IMAGES)
- Edit video (`video`, QUANTITY, VIDEOS)
- Quản trị Fanpage (`fanpage`, STATUS)
- Sáng tạo nội dung (`content`, STATUS)
- Báo cáo chạy ads (`ads`, STATUS)
- Báo cáo hiệu suất (`report`, STATUS)
- Ảnh bìa / Avatar (`cover`, STATUS)
- Like / Follow (`like`, STATUS)

## 3) Logic tính tiến độ

```text
totalItems = quotaPosts + quotaImages + quotaVideos
completedItems = min(completedPosts, quotaPosts)
               + min(completedImages, quotaImages)
               + min(completedVideos, quotaVideos)
overallPercent = completedItems / totalItems * 100
```

Ví dụ Basic 15:
- quotaPosts = 15, quotaImages = 15, quotaVideos = 0
- completedPosts = 13, completedImages = 10
- totalItems = 30
- completedItems = 23
- overallPercent = 77%

## 4) API response mẫu (`GET /api/v1/mobile/home`)

```json
{
  "subscription": {
    "id": "4e07b1ca-7cbe-4b06-9c9d-6d7f2d4f5d18",
    "packageCode": "BASIC_15",
    "tier": "BASIC",
    "displayTitle": "Gói 15 cơ bản",
    "startDate": "2026-05-01",
    "endDate": "2026-08-01",
    "status": "ACTIVE"
  },
  "progress": {
    "overallPercent": 77,
    "completedItems": 23,
    "totalItems": 30,
    "completedPosts": 13,
    "completedImages": 10,
    "completedVideos": 0,
    "quotaPosts": 15,
    "quotaImages": 15,
    "quotaVideos": 0,
    "status": "progress"
  },
  "services": [
    {
      "id": "posts",
      "name": "Viết bài",
      "icon": "edit",
      "trackMode": "quantity",
      "completedCount": 13,
      "totalCount": 15,
      "percent": 87,
      "status": "progress"
    },
    {
      "id": "design",
      "name": "Thiết kế hình ảnh",
      "icon": "image",
      "trackMode": "quantity",
      "completedCount": 10,
      "totalCount": 15,
      "percent": 67,
      "status": "progress"
    },
    {
      "id": "fanpage",
      "name": "Quản trị Fanpage",
      "icon": "doc",
      "trackMode": "status",
      "percent": 0,
      "status": "pending"
    }
  ]
}
```

## 5) UI dashboard mobile (đã triển khai)

- Tông xanh dương, card bo góc, đổ bóng nhẹ
- Card thông tin gói: tên gói, tier, thời gian, trạng thái
- Card tiến độ tổng thể: vòng tròn %, công thức `completed/total`, progress bar
- Danh sách dịch vụ:
  - `trackMode=quantity`: hiển thị `completed/total`, %, progress bar
  - `trackMode=status`: chỉ hiển thị trạng thái
- Icon dịch vụ và badge trạng thái rõ ràng
