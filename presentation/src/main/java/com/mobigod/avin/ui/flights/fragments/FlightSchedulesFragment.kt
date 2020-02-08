package com.mobigod.avin.ui.flights.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobigod.avin.databinding.FlightSchedulesLayoutBinding
import com.mobigod.avin.models.airport.AirportModel
import com.mobigod.avin.models.schedule.ScheduleModel
import com.mobigod.avin.states.State
import com.mobigod.avin.ui.adapters.FlightSchedulesAdapter
import com.mobigod.avin.ui.flights.FlightViewModel
import com.mobigod.avin.utils.hide
import com.mobigod.avin.utils.show
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

/**Created by: Emmanuel Ozibo
//on: 07, 2020-02-07
//at: 18:42*/
class FlightSchedulesFragment: Fragment() {

    private lateinit var binding: FlightSchedulesLayoutBinding
    lateinit var viewmodel: FlightViewModel

    private val disposable = CompositeDisposable()

    private val scheduleAdapter = FlightSchedulesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = activity?.run {
            ViewModelProvider(this)[FlightViewModel::class.java]
        } ?: throw Exception("Invalid activity, it doesn't contain viewmodel of this type")

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FlightSchedulesLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.schedulesRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
        }


        setUpListeners()
        setUpLiveDataObservers()
        setUpRxObservers()

        viewmodel.getFlightSchedules()
    }


    private fun setUpRxObservers() {
        disposable += scheduleAdapter.clickPublisher.subscribe {
            flightModel ->
            val departureAirportCode = flightModel.DepartureModel.AirportCode
            val arrivalAirportCode = flightModel.ArrivalModel.AirportCode
            Toast.makeText(context, "$departureAirportCode - $arrivalAirportCode", Toast.LENGTH_LONG).show()
            viewmodel.getAirportsWithCodes(listOf(departureAirportCode, arrivalAirportCode))
        }
    }


    private fun setUpLiveDataObservers() {
        viewmodel.flightDateSyncLiveData.observe(viewLifecycleOwner, Observer {
            binding.setDepartureDate(it)
        })

        viewmodel.originSyncLiveData.observe(viewLifecycleOwner, Observer {origin->
            binding.origin = origin
        })

        viewmodel.destinationSyncLiveData.observe(viewLifecycleOwner, Observer {destination ->
            binding.destination = destination
        })


        viewmodel.flightSchedulesLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                res -> when(res.state){
                State.LOADING -> loadingState()
                State.ERROR -> errorState(res.message)
                State.SUCCESS -> successState(res.data)
            }
            }
        })

        viewmodel.airportsWithCodeLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                resources -> when(resources.state) {
                State.LOADING -> loadingState()
                State.ERROR -> errorState(resources.message)
                State.SUCCESS -> lunchMapFragment(resources.data)
            }
            }
        })
    }


    /**
     * Lunch our maps fragment
     */
    private fun lunchMapFragment(data: List<AirportModel>?) {
        data?.run {

            data.forEach {
                airport -> Toast.makeText(context,
                "Latitude: ${airport.lat}, Longitude: ${airport.lon}", Toast.LENGTH_LONG).show()
            }

            val arrayList = arrayListOf<AirportModel>()
                .apply {addAll(data)  }

            val bundle = Bundle().apply { putParcelableArrayList("", arrayList) }
        }
    }


    private fun successState(data: List<ScheduleModel>?) {
        binding.progressBar.hide()
        binding.errorLayout.hide()
        binding.schedulesRv.show()
        binding.flightSchedules.hide()

        data?.run { scheduleAdapter.addSchedules(data) }
    }


    private fun loadingState() {
        binding.progressBar.show()
        binding.errorLayout.hide()
        binding.schedulesRv.hide()
        binding.flightSchedules.show()
    }

    private fun errorState(message: String?) {
        binding.errTv.text = message
        binding.progressBar.hide()
        binding.errorLayout.show()
        binding.schedulesRv.hide()
        binding.flightSchedules.hide()
    }



    private fun setUpListeners() {

    }


}