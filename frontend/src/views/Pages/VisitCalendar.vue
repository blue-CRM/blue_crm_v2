<template>
  <AdminLayout>
    <PageBreadcrumb pageTitle="내방일정 스케쥴표" />

    <div class="grid grid-cols-12 gap-4 min-w-0">
      <div class="col-span-12 space-y-6 min-w-0">
        <div
            class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
        >
          <!-- 상단 툴바 -->
          <div
              class="flex items-center justify-between gap-2 px-4 py-3 border-b border-gray-200 dark:border-gray-800 bg-white/80 dark:bg-gray-900/60"
          >
            <div class="flex items-center gap-2">
              <button
                  :class="btnSoft"
                  @click="goPrevWeek"
              >
                < 이전주
              </button>
              <button
                  :class="btnSoft"
                  @click="goToday"
              >
                오늘
              </button>
              <button
                  :class="btnSoft"
                  @click="goNextWeek"
              >
                다음주 >
              </button>
            </div>

            <div class="font-semibold text-gray-700 dark:text-white/70">
              {{ weekTitle }}
            </div>

            <button
                :class="btnSoftPrimary"
                @click="openManualAdd"
            >
              일정 추가 +
            </button>
          </div>

          <!-- 스크롤 호스트 -->
          <div
              ref="scrollHost"
              class="relative max-w-full overflow-x-auto overflow-y-auto custom-scrollbar"
              :style="{ maxHeight: scrollMaxHeight }"
          >
            <!-- 전체 그리드 -->
            <div class="min-w-max" :style="{ width: totalWidth + 'px' }">
              <!-- 헤더 (sticky) -->
              <div class="sticky top-0 z-40 bg-white dark:bg-gray-900">
                <!-- 날짜 그룹 헤더 -->
                <div
                    class="grid border-b border-gray-200 dark:border-gray-800"
                    :style="{ gridTemplateColumns: gridTemplateColumns }"
                >
                  <div
                      class="sticky left-0 z-50 flex items-center justify-center h-10 border-r border-gray-200
                           bg-slate-50 text-slate-700 text-xs font-semibold dark:bg-gray-900/60 dark:text-slate-200"
                  >
                    시간
                  </div>

                  <template v-for="(d, dayIdx) in days" :key="d.key">
                    <div
                        class="h-10 flex items-center justify-center text-sm font-semibold"
                        :style="{ gridColumn: `span ${ROOMS_PER_DAY}` }"
                        :class="[
                        dayHeaderClass(d.key),
                        dayIdx === days.length - 1 ? '' : 'border-r-2 border-gray-300 dark:border-gray-700'
                      ]"
                    >
                      {{ d.label }}
                    </div>
                  </template>
                </div>

                <!-- 룸 헤더 -->
                <div
                    class="grid border-b border-gray-200 dark:border-gray-800"
                    :style="{ gridTemplateColumns: gridTemplateColumns }"
                >
                  <div
                      class="sticky left-0 z-50 h-9 border-r border-gray-200
                           bg-slate-200 dark:bg-slate-800 dark:border-gray-800"
                  />

                  <template v-for="(col, colIdx) in columns" :key="col.key">
                    <div
                        class="h-9 flex items-center justify-center text-xs font-medium border-r border-gray-200 dark:border-gray-800"
                        :class="[
                        roomHeaderClass(col.dayKey),
                        col.isDayEnd ? 'border-r-2 border-gray-300 dark:border-gray-700' : ''
                      ]"
                    >
                      {{ col.roomLabel }}
                    </div>
                  </template>
                </div>
              </div>

              <!-- 바디 -->
              <div class="relative">
                <!-- 선택 드래그 오버레이 -->
                <div
                    v-if="selectionVisible"
                    class="absolute inset-y-0 right-0 z-20 pointer-events-none"
                    :style="{ left: TIME_COL_WIDTH + 'px', width: colCount * COL_WIDTH + 'px' }"
                >
                  <div class="absolute" :style="selectionRectStyle">
                    <div
                        class="h-full w-full rounded-xl border border-blue-500/60 bg-blue-500/10
                             ring-2 ring-blue-400/15 shadow-[0_6px_16px_rgba(37,99,235,0.10)]
                             backdrop-blur-[1px]"
                    />
                  </div>
                </div>

                <!-- 이벤트 레이어: 타임컬럼 제외 -->
                <div
                    class="absolute inset-y-0 z-30 pointer-events-none"
                    :style="{ left: TIME_COL_WIDTH + 'px', width: (colCount * COL_WIDTH) + 'px' }"
                >
                  <div
                      v-for="ev in visibleEvents"
                      :key="ev.id"
                      class="absolute pointer-events-auto select-none"
                      :style="eventStyle(ev)"
                      @pointerdown.stop.prevent="onEventPointerDown($event, ev)"
                      @click.stop="onEventClick(ev)"
                  >
                    <div
                        class="h-full w-full rounded-md shadow-sm border text-xs flex flex-col justify-between overflow-hidden"
                        :class="eventClass(ev)"
                    >
                      <div class="flex items-start justify-between gap-2 px-2 py-1">
                        <div class="min-w-0">
                          <div class="font-semibold truncate">
                            {{ ev.title }}
                          </div>
                          <div class="text-[11px] opacity-80">
                            {{ timeLabel(ev.startSlot) }} - {{ timeLabel(ev.endSlot) }}
                          </div>
                        </div>

                        <button
                            class="shrink-0 w-5 h-5 rounded hover:bg-black/10 dark:hover:bg-white/10 flex items-center justify-center"
                            @click.stop="deleteEvent(ev.id)"
                            title="삭제"
                        >
                          ✕
                        </button>
                      </div>

                      <div class="px-2 pb-1 text-[11px] opacity-80">
                        {{ ev.dayLabel }} · {{ ev.room }}룸
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 행들 -->
                <div
                    v-for="slotIdx in slots"
                    :key="slotIdx"
                    class="grid"
                    :style="{ gridTemplateColumns: gridTemplateColumns, height: ROW_HEIGHT + 'px' }"
                >
                  <!-- 시간(좌측 sticky) -->
                  <div
                      class="sticky left-0 z-40 flex items-center justify-end pr-2 text-xs border-r border-gray-200 dark:border-gray-800"
                      :class="timeColBgClass(slotIdx)"
                      :style="rowTopBorderStyle(slotIdx)"
                  >
                    {{ timeLabel(slotIdx) }}
                  </div>

                  <!-- 셀들 -->
                  <template v-for="(col, colIdx) in columns" :key="col.key">
                    <div
                        class="border-r border-gray-200 dark:border-gray-800"
                        data-cal-cell="1"
                        :data-col="colIdx"
                        :data-slot="slotIdx"
                        :class="[
                        cellBgClass(slotIdx, col.dayKey),
                        col.isDayEnd ? 'border-r-2 border-gray-300 dark:border-gray-700' : ''
                      ]"
                        :style="rowTopBorderStyle(slotIdx)"
                        @pointerdown.stop.prevent="onCellPointerDown($event, colIdx, slotIdx)"
                    />
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- 이벤트 모달 -->
          <div
              v-if="isOpen"
              ref="modalRoot"
              class="fixed inset-0 z-[100000] flex items-center justify-center bg-black/40 p-4"
              tabindex="0"
              @click.self="closeModal"
              @keydown.esc.prevent.stop="closeModal"
          >
            <div class="w-full max-w-[700px] rounded-2xl bg-white dark:bg-gray-900 shadow-xl border border-gray-200 dark:border-gray-800">
              <!-- 헤더 -->
              <div class="px-5 py-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
                <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100">
                  {{ editingEventId ? '내방 일정 수정' : '내방 일정 추가' }}
                </h3>
                <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300" @click="closeModal">✕</button>
              </div>

              <!-- 바디 -->
              <div class="px-5 py-4 space-y-4">
                <!-- 시간/날짜 한 줄 -->
                <div class="rounded-xl border border-gray-200 p-3 text-sm text-gray-700 dark:border-gray-800 dark:text-gray-200">
                  일정: <b>{{ pendingDateTimeLabel }}</b>
                </div>

                <!-- 회의실 선택 (API 로드) -->
                <div class="space-y-1">
                  <label class="block text-sm text-gray-600 dark:text-gray-300">회의실</label>
                  <select
                      v-model.number="pendingRoom"
                      class="w-full h-11 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2
                 text-sm focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                 dark:bg-gray-800 dark:text-gray-200"
                  >
                    <option :value="null" disabled>회의실을 선택하세요</option>
                    <option v-for="r in rooms" :key="r.room" :value="r.room">
                      {{ r.label }}
                    </option>
                  </select>
                </div>

                <!-- 일정명 -->
                <div class="space-y-1">
                  <label class="block text-sm text-gray-600 dark:text-gray-300">일정명</label>
                  <input
                      v-model.trim="eventTitle"
                      type="text"
                      placeholder="예: 유현우 / 내부 미팅"
                      class="h-11 w-full rounded-lg border px-3
                 bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                  />
                </div>

                <!-- 고객 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <label class="block text-sm text-gray-600 dark:text-gray-300">고객</label>

                    <button
                        v-if="!isCustomerLocked && pickedCustomer"
                        class="text-xs px-2 py-1 rounded-md border border-gray-200 hover:bg-gray-50
                   dark:border-gray-700 dark:hover:bg-gray-800/70"
                        @click="clearPickedCustomer"
                    >
                      선택 해제
                    </button>
                  </div>

                  <!-- 고정 고객(쿼리 customerId) 또는 선택된 고객 표시 -->
                  <div v-if="pickedCustomer" class="rounded-xl border border-gray-200 p-3 dark:border-gray-800">
                    <div class="flex items-center justify-between gap-3">
                      <div class="min-w-0">
                        <div class="text-sm text-gray-800 dark:text-gray-100 truncate">
                          <b>{{ pickedCustomer.customerName }}</b>
                          <span class="text-gray-400 ml-2">{{ pickedCustomer.customerPhone }}</span>
                        </div>
                        <div class="text-xs text-gray-500 dark:text-gray-400">
                          생성일: {{ pickedCustomer.customerCreatedAt }}
                        </div>
                      </div>

                      <span v-if="isCustomerLocked" class="text-[11px] px-2 py-1 rounded-md bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-200">
                      고정
                    </span>
                    </div>
                  </div>

                  <!-- customerId 없을 때만: 고객 검색/선택 -->
                  <template v-if="!isCustomerLocked">
                    <div class="flex gap-2">
                      <input
                          v-model.trim="customerQuery"
                          @input="debouncedCustomerSearch"
                          type="text"
                          placeholder="이름 또는 전화번호로 검색"
                          class="h-11 w-full rounded-lg border px-3
                     bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                      />
                      <button
                          @click="searchCustomers"
                          :disabled="submitting"
                          class="inline-flex h-11 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm
                     hover:bg-gray-50 disabled:opacity-60
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:hover:bg-gray-700/60"
                      >
                        검색
                      </button>
                    </div>

                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      내방 상태이며 예약이 없는 고객만 표시됩니다.
                    </p>

                    <div
                        v-if="customerResults.length"
                        class="max-h-52 overflow-auto border border-gray-100 dark:border-gray-800 rounded-md"
                    >
                      <ul>
                        <li
                            v-for="c in customerResults"
                            :key="c.customerId"
                            @click="selectCustomer(c)"
                            class="flex items-center justify-between px-3 py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800"
                            :class="pickedCustomer?.customerId === c.customerId ? 'bg-gray-100 dark:bg-gray-800/70' : ''"
                        >
                          <div class="min-w-0">
                            <div class="text-sm text-gray-700 dark:text-gray-200 truncate">
                              <b>{{ c.customerName }}</b>
                              <span class="text-gray-400 ml-2">{{ c.customerPhone }}</span>
                            </div>
                            <div class="text-xs text-gray-500 dark:text-gray-400">
                              생성일: {{ c.customerCreatedAt }}
                            </div>
                          </div>

                          <button
                              class="px-2 py-1 text-xs rounded-md border transition"
                              @click.stop="selectCustomer(c)"
                              :class="pickedCustomer?.customerId === c.customerId
                    ? 'bg-gray-900 text-white border-gray-900 hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:border-white dark:hover:bg-gray-200'
                    : 'text-gray-700 border-gray-300 hover:bg-gray-50 dark:text-gray-300 dark:border-gray-700 dark:hover:bg-gray-800/70'"
                          >
                            {{ pickedCustomer?.customerId === c.customerId ? '선택됨' : '선택' }}
                          </button>
                        </li>
                      </ul>
                    </div>

                    <div v-else class="text-xs text-gray-400 dark:text-gray-500">
                      검색 결과가 없습니다.
                    </div>
                  </template>

                  <!-- customerId 있는데 아직 로드 전 -->
                  <div v-if="isCustomerLocked && !pickedCustomer" class="text-xs text-gray-500 dark:text-gray-400">
                    고객 정보를 불러오는 중...
                  </div>
                </div>
              </div>

              <!-- 푸터 -->
              <div class="px-5 py-3 border-t border-gray-200 dark:border-gray-800 flex items-center justify-end gap-2">
                <button
                    class="h-10 px-4 rounded-lg border border-gray-200 text-sm hover:bg-gray-50
               dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:hover:bg-gray-700/60"
                    @click="closeModal"
                >
                  취소
                </button>

                <button
                    class="h-10 px-4 rounded-lg bg-blue-600 text-white text-sm disabled:opacity-60"
                    :disabled="submitting || !canSave"
                    @click="saveEvent"
                >
                  {{ submitting ? '처리 중...' : (editingEventId ? '수정 저장' : '추가') }}
                </button>

                <button
                    v-if="editingEventId"
                    class="h-10 px-4 rounded-lg border border-red-500 bg-red-500 text-white text-sm hover:bg-red-600 disabled:opacity-60"
                    :disabled="submitting"
                    @click="deleteEvent(editingEventId)"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import axios from '@/plugins/axios'
import { useAuthStore } from "@/stores/auth.js";
import { useRoute } from 'vue-router'

const auth = useAuthStore();
const role = auth.grants.role;
const route = useRoute()

// 버튼 색상
const btnSoft =
    'h-9 px-3 rounded-xl border text-sm font-medium transition ' +
    'border-gray-200 bg-white text-gray-700 shadow-sm ' +
    'hover:bg-gray-50 hover:border-gray-300 ' +
    'focus:outline-none focus:ring-2 focus:ring-blue-500/20 ' +
    'dark:border-gray-800 dark:bg-gray-900/40 dark:text-gray-200 dark:shadow-none ' +
    'dark:hover:bg-white/[0.03]'

const btnSoftPrimary =
    'h-9 px-3 rounded-xl text-sm font-semibold transition ' +
    'bg-blue-600 text-white shadow-sm ' +
    'hover:bg-blue-700 ' +
    'focus:outline-none focus:ring-2 focus:ring-blue-500/30 ' +
    'dark:bg-blue-600 dark:hover:bg-blue-500'

/** ===== 설정 ===== */
const ROOMS_PER_DAY = 5
const ROW_HEIGHT = 40
const TIME_COL_WIDTH = 100
const COL_WIDTH = 130

const START_HOUR = 8
const SLOT_MINUTES = 30

const START_MINUTES = START_HOUR * 60
const END_MINUTES_EXCLUSIVE = (22 * 60) + 30
const slotCount = (END_MINUTES_EXCLUSIVE - START_MINUTES) / SLOT_MINUTES

/** ===== 날짜 유틸 ===== */
function pad2(n: number) { return String(n).padStart(2, '0') }
function ymd(d: Date) { return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` }
function addDays(d: Date, days: number) { const x = new Date(d); x.setDate(x.getDate() + days); return x }
function startOfWeek(d: Date) {
  const x = new Date(d)
  const day = x.getDay() // 0=일
  const diff = (day === 0 ? -6 : 1 - day) // 월요일 시작
  x.setDate(x.getDate() + diff)
  x.setHours(0, 0, 0, 0)
  return x
}
function koDow(d: Date) { return ['일', '월', '화', '수', '목', '금', '토'][d.getDay()] }

/** ===== 상태 ===== */
const todayKey = ref(ymd(new Date()))
const weekStart = ref(startOfWeek(new Date()))

const days = computed(() => {
  const out: Array<{ key: string; date: Date; label: string }> = []
  for (let i = 0; i < 7; i++) {
    const dt = addDays(weekStart.value, i)
    out.push({
      key: ymd(dt),
      date: dt,
      label: `${pad2(dt.getMonth() + 1)}.${pad2(dt.getDate())} ${koDow(dt)}`
    })
  }
  return out
})

type Column = { key: string; dayKey: string; dayLabel: string; room: number; roomLabel: string; isDayEnd: boolean }
const columns = computed<Column[]>(() => {
  const out: Column[] = []
  for (const d of days.value) {
    for (let r = 1; r <= ROOMS_PER_DAY; r++) {
      out.push({
        key: `${d.key}#${r}`,
        dayKey: d.key,
        dayLabel: d.label,
        room: r,
        roomLabel: `${r}룸`,
        isDayEnd: r === ROOMS_PER_DAY
      })
    }
  }
  return out
})

const colCount = computed(() => columns.value.length)
const totalWidth = computed(() => TIME_COL_WIDTH + colCount.value * COL_WIDTH)
const gridTemplateColumns = computed(() => `${TIME_COL_WIDTH}px repeat(${colCount.value}, ${COL_WIDTH}px)`)

const weekTitle = computed(() => {
  const s = days.value[0]?.date
  const e = days.value[6]?.date
  if (!s || !e) return ''
  return `${s.getFullYear()}.${pad2(s.getMonth()+1)}.${pad2(s.getDate())} - ${e.getFullYear()}.${pad2(e.getMonth()+1)}.${pad2(e.getDate())}`
})

const slots = computed(() => Array.from({ length: slotCount }, (_, i) => i))

/** ===== 색/배경 ===== */
function dayHeaderClass(dayKey: string) {
  return dayKey === todayKey.value
      ? 'bg-amber-100/80 text-amber-900 dark:bg-amber-900/35 dark:text-amber-200' // 오늘
      : 'bg-blue-100/80 text-blue-900 dark:bg-blue-900/35 dark:text-blue-200' // 다른날
}
function roomHeaderClass(dayKey: string) {
  return dayKey === todayKey.value
      ? 'bg-amber-50 text-amber-900 dark:bg-amber-900/20 dark:text-amber-200' // 오늘
      : 'bg-blue-50 text-blue-900 dark:bg-blue-900/20 dark:text-blue-200' // 다른날
}
function cellBgClass(slotIdx: number, dayKey: string) {
  const isZebraEven = slotIdx % 2 === 0
  return isZebraEven ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-800/60'
  // if (dayKey !== todayKey.value) {
  //   return isZebraEven ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-800/60'
  // }
  // return isZebraEven ? 'bg-amber-50/80 dark:bg-amber-900/15' : 'bg-amber-100/60 dark:bg-amber-900/25'
}
function timeColBgClass(slotIdx: number) {
  return slotIdx % 2 === 0
      ? 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
      : 'bg-slate-200 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
}

/** ===== 스크롤 높이 + Ctrl+휠 가로스크롤 ===== */
const scrollHost = ref<HTMLElement | null>(null)
const scrollMaxHeight = ref('520px')

function updateScrollHeight() {
  const el = scrollHost.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const viewportHeight = window.innerHeight || document.documentElement.clientHeight
  const bottomPadding = 110
  let h = viewportHeight - rect.top - bottomPadding
  if (h < 260) h = 260
  scrollMaxHeight.value = `${h}px`
}

function handleHorizontalWheel(e: WheelEvent) {
  const el = scrollHost.value
  if (!el) return
  if (!e.ctrlKey) return
  e.preventDefault()
  const base = e.deltaY !== 0 ? e.deltaY : e.deltaX
  const factor = (e as any).deltaMode === 1 ? 20 : 1
  el.scrollLeft += base * factor
}

function scrollToDay(dayKey: string) {
  const host = scrollHost.value
  if (!host) return
  const idx = days.value.findIndex(d => d.key === dayKey)
  if (idx < 0) { host.scrollLeft = 0; return }
  const target = idx * ROOMS_PER_DAY * COL_WIDTH
  host.scrollLeft = target
}

onMounted(() => {
  updateScrollHeight()
  window.addEventListener('resize', updateScrollHeight)
  window.addEventListener('orientationchange', updateScrollHeight)

  const host = scrollHost.value
  if (host) host.addEventListener('wheel', handleHorizontalWheel, { passive: false })

  requestAnimationFrame(() => scrollToDay(todayKey.value))
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScrollHeight)
  window.removeEventListener('orientationchange', updateScrollHeight)
  const host = scrollHost.value
  if (host) host.removeEventListener('wheel', handleHorizontalWheel as any)
})

watch(weekStart, async () => {
  await nextTick()
  scrollToDay(todayKey.value)
})

/** ===== 시간 라벨 / 정오 라인 ===== */
function timeLabel(slotIdx: number) {
  const totalMin = START_MINUTES + slotIdx * SLOT_MINUTES
  const hh = Math.floor(totalMin / 60)
  const mm = totalMin % 60
  const ap = hh < 12 ? '오전' : '오후'
  const hh12 = hh % 12 === 0 ? 12 : hh % 12
  return `${ap} ${hh12}시${mm === 0 ? '' : ' 30분'}`
}
function rowTopBorderStyle(slotIdx: number) {
  const noonSlot = ((12 * 60) - START_MINUTES) / SLOT_MINUTES
  if (slotIdx === noonSlot) {
    return { borderTop: '2px solid rgb(209 213 219)' }
  }
  return {}
}

/** ===== 선택 드래그 (가로+세로 스윕) ===== */
const selecting = ref(false)
const selection = reactive({ colFrom: -1, colTo: -1, slotFrom: -1, slotTo: -1 })

function normalizeSelection() {
  const c1 = Math.min(selection.colFrom, selection.colTo)
  const c2 = Math.max(selection.colFrom, selection.colTo)
  const s1 = Math.min(selection.slotFrom, selection.slotTo)
  const s2 = Math.max(selection.slotFrom, selection.slotTo)
  return { c1, c2, s1, s2 }
}
function isSelected(colIdx: number, slotIdx: number) {
  if (!selecting.value && selection.colFrom < 0) return false
  const { c1, c2, s1, s2 } = normalizeSelection()
  return colIdx >= c1 && colIdx <= c2 && slotIdx >= s1 && slotIdx <= s2
}

function clearSelection() {
  selecting.value = false
  selection.colFrom = -1
  selection.colTo = -1
  selection.slotFrom = -1
  selection.slotTo = -1

  window.removeEventListener('pointermove', onSelectingPointerMove)
}

const selectionVisible = computed(() => selection.colFrom >= 0 && selection.slotFrom >= 0)

const selectionRectStyle = computed(() => {
  if (!selectionVisible.value) return {}
  const { c1, c2, s1, s2 } = normalizeSelection()
  return {
    left: `${c1 * COL_WIDTH}px`,
    top: `${s1 * ROW_HEIGHT}px`,
    width: `${(c2 - c1 + 1) * COL_WIDTH}px`,
    height: `${(s2 - s1 + 1) * ROW_HEIGHT}px`,
  }
})

function onCellPointerDown(_e: PointerEvent, colIdx: number, slotIdx: number) {
  selecting.value = true
  selection.colFrom = colIdx
  selection.colTo = colIdx
  selection.slotFrom = slotIdx
  selection.slotTo = slotIdx

  window.addEventListener('pointermove', onSelectingPointerMove, { passive: true })
  window.addEventListener('pointerup', onSelectingPointerUp, { once: true })
}
function onSelectingPointerMove(e: PointerEvent) {
  if (!selecting.value) return
  const el = document.elementFromPoint(e.clientX, e.clientY) as HTMLElement | null
  const cell = el?.closest?.('[data-cal-cell="1"]') as HTMLElement | null
  if (!cell) return

  const c = Number(cell.dataset.col)
  const s = Number(cell.dataset.slot)
  if (!Number.isFinite(c) || !Number.isFinite(s)) return

  selection.colTo = c
  selection.slotTo = s
}
function onSelectingPointerUp() {
  if (!selecting.value) return

  // 선택 범위 먼저 확보
  const range = normalizeSelection()

  // 오버레이 즉시 제거
  clearSelection()

  // 모달 오픈 (range로)
  openModalFromSelection(range)
}

/** ===== 이벤트 모델 ===== */
const lockedCustomerId = computed(() => {
  const raw = route.query.customerId
  const v = Array.isArray(raw) ? raw[0] : raw
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : null
})
const isCustomerLocked = computed(() => lockedCustomerId.value != null)

const rooms = ref<RoomOption[]>([])
const customerQuery = ref('')
const customerResults = ref<CustomerPick[]>([])
const pickedCustomer = ref<CustomerPick | null>(null)

const submitting = ref(false)
const modalRoot = ref<HTMLElement | null>(null)

const pendingDayKey = ref<string>('')     // 선택된 날짜 key (YYYY-MM-DD)
const pendingDayLabel = ref<string>('')   // 선택된 라벨 (MM.DD 요일)
const pendingSlot = ref<number>(0)        // 선택된 슬롯 (startSlot)
const pendingRoom = ref<number | null>(null)

const pendingDateTimeLabel = computed(() => {
  const day = pendingDayLabel.value || pendingDayKey.value
  return `${day} · ${timeLabel(pending.s1)} - ${timeLabel(pending.s2 + 1)}`
})

const canSave = computed(() => {
  return !!pendingDayKey.value && pendingRoom.value != null && eventTitle.value.trim().length > 0
})

type ScheduleEvent = {
  id: string
  title: string
  dayKey: string
  dayLabel: string
  room: number
  startSlot: number
  endSlot: number // exclusive
}

function dayRoomFromColIndex(colIdx: number) {
  const dayIdx = Math.floor(colIdx / ROOMS_PER_DAY)
  const room = (colIdx % ROOMS_PER_DAY) + 1
  const dayKey = days.value[dayIdx]?.key
  const dayLabel = days.value[dayIdx]?.label
  return { dayKey, dayLabel, room }
}
function colIndexOf(dayKey: string, room: number) {
  const dayIdx = days.value.findIndex(d => d.key === dayKey)
  if (dayIdx < 0) return -1
  return dayIdx * ROOMS_PER_DAY + (room - 1)
}

const events = ref<ScheduleEvent[]>([
  // 샘플
  (() => {
    const dk = todayKey.value
    const dlabel = days.value.find(d => d.key === dk)?.label ?? dk
    return { id: '1', title: '유현우', dayKey: dk, dayLabel: dlabel, room: 1, startSlot: 4, endSlot: 6 }
  })()
])

const visibleEvents = computed(() => {
  const daySet = new Set(days.value.map(d => d.key))
  return events.value.filter(e => daySet.has(e.dayKey))
})

function roomColorClass(room: number) {
  switch (room) {
    case 1: return 'bg-fuchsia-500/15 border-fuchsia-500/40 text-fuchsia-900 dark:text-fuchsia-200'
    case 2: return 'bg-red-500/15 border-red-500/40 text-red-900 dark:text-red-200'
    case 3: return 'bg-yellow-500/20 border-yellow-500/50 text-yellow-900 dark:text-yellow-200'
    case 4: return 'bg-blue-500/15 border-blue-500/40 text-blue-900 dark:text-blue-200'
    case 5: return 'bg-emerald-500/15 border-emerald-500/40 text-emerald-900 dark:text-emerald-200'
    default: return 'bg-gray-500/10 border-gray-400/40 text-gray-800 dark:text-gray-200'
  }
}
function eventClass(ev: ScheduleEvent) {
  return `border ${roomColorClass(ev.room)}`
}
function eventStyle(ev: ScheduleEvent) {
  const colIdx = colIndexOf(ev.dayKey, ev.room)
  const left = colIdx * COL_WIDTH // 이벤트 레이어가 TIME_COL_WIDTH만큼 밀려있음
  const top = ev.startSlot * ROW_HEIGHT
  const height = Math.max(ROW_HEIGHT, (ev.endSlot - ev.startSlot) * ROW_HEIGHT)
  return {
    left: `${left}px`,
    top: `${top}px`,
    width: `${COL_WIDTH}px`,
    height: `${height}px`,
    padding: '2px',
    cursor: dragging.value?.id === ev.id ? 'grabbing' : 'grab'
  } as any
}

/** ===== 모달(추가/수정) ===== */
const isOpen = ref(false)
const eventTitle = ref('')
const editingEventId = ref<string | null>(null)

const pending = reactive({ c1: 0, c2: 0, s1: 0, s2: 0 })

const pendingStartLabel = computed(() => timeLabel(pending.s1))
const pendingEndLabel = computed(() => timeLabel(pending.s2 + 1))
const pendingColsLabel = computed(() => {
  const cols: string[] = []
  for (let c = pending.c1; c <= pending.c2; c++) {
    const { dayLabel, room } = dayRoomFromColIndex(c)
    cols.push(`${dayLabel} ${room}룸`)
  }
  return cols.join(', ')
})
const selectionSummary = computed(() => {
  if (editingEventId.value) return '드래그로 이동 가능, 여기서 제목 수정 및 삭제 가능'
  return '드래그로 선택한 구간과 열 전체에 동일 일정이 생성됨'
})

function closeModal() {
  isOpen.value = false
  editingEventId.value = null
  eventTitle.value = ''

  customerQuery.value = ''
  customerResults.value = []

  if (!isCustomerLocked.value) pickedCustomer.value = null

  clearSelection()
}

function openAddFromSelection() {
  const { c1, c2, s1, s2 } = normalizeSelection()
  if (c1 < 0 || s1 < 0) return

  pending.c1 = c1
  pending.c2 = c2
  pending.s1 = s1
  pending.s2 = s2

  editingEventId.value = null
  eventTitle.value = ''
  isOpen.value = true
}

function openManualAdd() {
  pending.c1 = 0
  pending.c2 = 0
  pending.s1 = 2
  pending.s2 = 2
  editingEventId.value = null
  eventTitle.value = ''
  isOpen.value = true
}

async function openEditEvent(ev: any) {
  pendingDayKey.value = ev.dayKey
  pendingDayLabel.value = ev.dayLabel
  pendingSlot.value = ev.startSlot
  pendingRoom.value = ev.room

  const col = colIndexOf(ev.dayKey, ev.room)
  pending.c1 = col >= 0 ? col : 0
  pending.c2 = pending.c1
  pending.s1 = ev.startSlot
  pending.s2 = Math.max(ev.startSlot, (ev.endSlot ?? ev.startSlot + 1) - 1)

  editingEventId.value = ev.id
  eventTitle.value = ev.title

  if (ev.customerId) {
    pickedCustomer.value = {
      customerId: ev.customerId,
      customerName: ev.customerName ?? '',
      customerPhone: ev.customerPhone ?? '',
      customerCreatedAt: ev.customerCreatedAt ?? ''
    }
  }

  await loadRooms()

  if (isCustomerLocked.value) {
    await loadLockedCustomer()
  }

  isOpen.value = true
  await nextTick()
  modalRoot.value?.focus?.()
}
async function openModalFromSelection(range: { c1: number; c2: number; s1: number; s2: number }) {
  const { c1, c2, s1, s2 } = range
  if (c1 < 0 || s1 < 0) return

  // 선택 범위 저장
  pending.c1 = c1
  pending.c2 = c2
  pending.s1 = s1
  pending.s2 = s2

  // 모달 표시용(시작셀 기준)
  const { dayKey, dayLabel, room } = dayRoomFromColIndex(c1)
  pendingDayKey.value = dayKey || ''
  pendingDayLabel.value = dayLabel || ''
  pendingSlot.value = s1
  pendingRoom.value = room

  await loadRooms()

  if (isCustomerLocked.value) {
    await loadLockedCustomer()
    const exist = events.value.find(e => (e as any).customerId === lockedCustomerId.value)
    if (exist) {
      await openEditEvent(exist as any)
      return
    }
  }

  editingEventId.value = null
  isOpen.value = true
  await nextTick()
  modalRoot.value?.focus?.()
}

function saveEvent() {
  const title = eventTitle.value.trim()
  if (!canSave.value) return

  // 세로 선택 범위 그대로 1개 이벤트로 합치기
  const startSlot = pending.s1
  const endSlot = pending.s2 + 1 // exclusive

  // 편집이면 1개만 수정
  if (editingEventId.value) {
    const id = editingEventId.value
    events.value = events.value.map((e: any) => {
      if (e.id !== id) return e
      return {
        ...e,
        title,
        dayKey: pendingDayKey.value,
        dayLabel: pendingDayLabel.value,
        room: pendingRoom.value!,
        startSlot,
        endSlot,
        customerId: pickedCustomer.value?.customerId ?? null,
        customerName: pickedCustomer.value?.customerName ?? null,
        customerPhone: pickedCustomer.value?.customerPhone ?? null
      }
    })
    closeModal()
    return
  }

  // 추가면: 가로로 여러 컬럼 선택했으면 컬럼마다 1개씩 생성(세로는 합쳐진 1개)
  const created: any[] = []
  for (let c = pending.c1; c <= pending.c2; c++) {
    const { dayKey, dayLabel, room } = dayRoomFromColIndex(c)
    if (!dayKey) continue

    created.push({
      id: `${Date.now()}-${dayKey}-${room}-${startSlot}-${c}`,
      title,
      dayKey,
      dayLabel: dayLabel || dayKey,
      room,
      startSlot,
      endSlot,
      customerId: pickedCustomer.value?.customerId ?? null,
      customerName: pickedCustomer.value?.customerName ?? null,
      customerPhone: pickedCustomer.value?.customerPhone ?? null
    })
  }

  events.value = [...events.value, ...created]
  closeModal()
}

function deleteEvent(id: string | null) {
  if (!id) return
  events.value = events.value.filter(e => e.id !== id)
  if (editingEventId.value === id) closeModal()
}

/** ===== 이벤트 드래그 이동 ===== */
const dragging = ref<null | {
  id: string
  startX: number
  startY: number
  baseCol: number
  baseStart: number
  baseEnd: number
  moved: boolean
}>(null)

function clamp(n: number, min: number, max: number) { return Math.max(min, Math.min(max, n)) }

function onEventPointerDown(e: PointerEvent, ev: ScheduleEvent) {
  const col = colIndexOf(ev.dayKey, ev.room)
  dragging.value = {
    id: ev.id,
    startX: e.clientX,
    startY: e.clientY,
    baseCol: col,
    baseStart: ev.startSlot,
    baseEnd: ev.endSlot,
    moved: false
  }

  try { (e.currentTarget as HTMLElement)?.setPointerCapture?.(e.pointerId) } catch {}

  window.addEventListener('pointermove', onEventPointerMove, { passive: true })
  window.addEventListener('pointerup', onEventPointerUp, { once: true })
}

function onEventPointerMove(e: PointerEvent) {
  const d = dragging.value
  if (!d) return

  const dx = e.clientX - d.startX
  const dy = e.clientY - d.startY
  if (Math.abs(dx) + Math.abs(dy) > 2) d.moved = true

  const colDelta = Math.round(dx / COL_WIDTH)
  const slotDelta = Math.round(dy / ROW_HEIGHT)

  const duration = d.baseEnd - d.baseStart
  const nextCol = clamp(d.baseCol + colDelta, 0, colCount.value - 1)
  const nextStart = clamp(d.baseStart + slotDelta, 0, slotCount - duration)
  const nextEnd = nextStart + duration

  events.value = events.value.map(ev => {
    if (ev.id !== d.id) return ev
    const { dayKey, dayLabel, room } = dayRoomFromColIndex(nextCol)
    return {
      ...ev,
      dayKey: dayKey || ev.dayKey,
      dayLabel: dayLabel || ev.dayLabel,
      room,
      startSlot: nextStart,
      endSlot: nextEnd
    }
  })
}

function onEventPointerUp() {
  window.removeEventListener('pointermove', onEventPointerMove)
  // 클릭-열기 방지용 플래그는 onEventClick에서 확인
  setTimeout(() => { dragging.value = null }, 0)
}

function onEventClick(ev: ScheduleEvent) {
  // 드래그 직후 click가 따라오는 케이스 방지
  if (dragging.value?.id === ev.id && dragging.value?.moved) return
  openEditEvent(ev)
}

/** ===== 주 이동 ===== */
function goPrevWeek() { weekStart.value = addDays(weekStart.value, -7) }
function goNextWeek() { weekStart.value = addDays(weekStart.value, 7) }
function goToday() {
  todayKey.value = ymd(new Date())
  weekStart.value = startOfWeek(new Date())
}

/** ====== api호출 ===== */
async function loadRooms() {
  try {
    const { data } = await axios.get('/api/work/visit/rooms')
    const arr = Array.isArray(data) ? data : []
    rooms.value = arr.map((r: any, idx: number) => {
      const roomNo = Number(r.room ?? r.roomNo ?? r.no ?? r.id ?? (idx + 1))
      return {
        room: roomNo,
        label: r.roomName ?? r.name ?? `${roomNo}룸`
      }
    })
    if (!rooms.value.length) throw new Error('empty rooms')
  } catch (e) {
    // 실패하면 기본 1~5룸 fallback
    rooms.value = Array.from({ length: ROOMS_PER_DAY }, (_, i) => ({ room: i + 1, label: `${i + 1}룸` }))
  }
}

async function loadLockedCustomer() {
  if (!isCustomerLocked.value) return
  if (pickedCustomer.value?.customerId === lockedCustomerId.value) return

  try {
    const { data } = await axios.get(`/api/work/visit/customers/${lockedCustomerId.value}`)
    pickedCustomer.value = {
      customerId: data.customerId,
      customerName: data.customerName,
      customerPhone: data.customerPhone,
      customerCreatedAt: data.customerCreatedAt
    }
    if (!eventTitle.value.trim()) eventTitle.value = pickedCustomer.value.customerName
  } catch (e) {
    console.error(e)
  }
}

function mapCustomer(c: any): CustomerPick {
  return {
    customerId: Number(c.customerId ?? c.id),
    customerName: String(c.customerName ?? c.name ?? ''),
    customerPhone: String(c.customerPhone ?? c.phone ?? ''),
    customerCreatedAt: String(c.customerCreatedAt ?? c.createdAt ?? '')
  }
}

async function searchCustomers() {
  try {
    if (!customerQuery.value) {
      customerResults.value = []
      return
    }
    const { data } = await axios.get('/api/work/visit/customers', {
      params: { q: customerQuery.value }
      // 서버에서: status=내방 AND reservation_at IS NULL 같은 조건으로 필터링
    })
    customerResults.value = (Array.isArray(data) ? data : []).map(mapCustomer)
  } catch (e) {
    console.error(e)
  }
}

let ct: any = null
function debouncedCustomerSearch() {
  clearTimeout(ct)
  ct = setTimeout(searchCustomers, 250)
}

function selectCustomer(c: CustomerPick) {
  pickedCustomer.value = c
  if (!eventTitle.value.trim()) eventTitle.value = c.customerName
}

function clearPickedCustomer() {
  if (isCustomerLocked.value) return
  pickedCustomer.value = null
}

async function openModalFromCell(colIdx: number, slotIdx: number) {
  // 선택 범위 저장 (1칸)
  pending.c1 = colIdx
  pending.c2 = colIdx
  pending.s1 = slotIdx
  pending.s2 = slotIdx

  const { dayKey, dayLabel, room } = dayRoomFromColIndex(colIdx)
  pendingDayKey.value = dayKey || ''
  pendingDayLabel.value = dayLabel || ''
  pendingSlot.value = slotIdx
  pendingRoom.value = room

  await loadRooms()

  if (isCustomerLocked.value) {
    await loadLockedCustomer()
    const exist = events.value.find(e => (e as any).customerId === lockedCustomerId.value)
    if (exist) {
      openEditEvent(exist as any)
      return
    }
  }

  editingEventId.value = null
  isOpen.value = true
  await nextTick()
  modalRoot.value?.focus?.()
}

</script>
