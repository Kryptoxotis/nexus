'use client'

import type { Business } from '@/lib/types'

interface BusinessCardProps {
  business: Business
  isOwner?: boolean
}

export default function BusinessCard({ business, isOwner }: BusinessCardProps) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-sm transition-shadow">
      <div className="flex items-start gap-4">
        <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center flex-shrink-0">
          <span className="text-xl font-bold text-blue-600">
            {business.name.charAt(0).toUpperCase()}
          </span>
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <h3 className="font-semibold text-gray-900">{business.name}</h3>
            {isOwner && (
              <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-purple-100 text-purple-800">
                Owner
              </span>
            )}
            {!business.is_active && (
              <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-gray-100 text-gray-600">
                Inactive
              </span>
            )}
          </div>
          {business.description && (
            <p className="text-sm text-gray-600 mt-1">{business.description}</p>
          )}
          {business.category && (
            <span className="inline-block mt-2 px-2 py-0.5 text-xs font-medium rounded-full bg-gray-100 text-gray-600">
              {business.category}
            </span>
          )}
        </div>
      </div>
    </div>
  )
}
